/*
 * Author: Santiago Ontañón Villar (Brain Games)
 */
package test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import cl.MDLConfig;
import cl.OptimizationResult;
import code.CodeBase;
import code.SourceConstant;
import code.SourceFile;
import code.CodeStatement;
import java.util.ArrayList;
import java.util.List;
import workers.AnnotatedSourceCodeGenerator;
import workers.DataOptimizer;

/**
 *
 * @author santi
 */
public class DataOptimizerTest {

    private final MDLConfig config;
    private final CodeBase code;
    private final DataOptimizer worker;

    public DataOptimizerTest() {
        config = new MDLConfig();
        worker = new DataOptimizer(config);
        config.registerWorker(worker);
        code = new CodeBase(config);
    }

    @Test public void test1() throws IOException { test("data/dotests/test1.asm", 1, 4); }

    
    private void test(String inputFile, int nDataOptimizations, int expectedSavedBytes) throws IOException
    {
        Assert.assertTrue(config.parseArgs(inputFile, "-do"));
        Assert.assertTrue(
                "Could not parse file " + inputFile,
                config.codeBaseParser.parseMainSourceFile(config.inputFile, code));        
        testInternal(nDataOptimizations, expectedSavedBytes);
    }
    

    private void testInternal(int nDataOptimizations, int expectedSavedBytes) throws IOException
    {
        // Make sure we don't lose any labels:
        List<SourceConstant> labelsBefore = new ArrayList<>();
        List<SourceConstant> labelsAfter = new ArrayList<>();
        for(SourceFile f:code.getSourceFiles()) {
            for(CodeStatement s:f.getStatements()) {
                if (s.label != null && s.label.isLabel()) {
                    labelsBefore.add(s.label);
                }
            }
        }
                
        worker.work(code);
        OptimizationResult r = config.optimizerStats;
        
        for(SourceFile f:code.getSourceFiles()) {
            for(CodeStatement s:f.getStatements()) {
                if (s.label != null && s.label.isLabel()) {
                    labelsAfter.add(s.label);
                }
            }
        }
        
        Assert.assertEquals(labelsBefore.size(), labelsAfter.size());
        Integer nOptimizations = r.optimizerSpecificStats.get(DataOptimizer.DATA_OPTIMIZER_OPTIMIZATIONS_CODE);
        if (nOptimizations == null) nOptimizations = 0;
        Assert.assertEquals("r.nDataOptimizations", nDataOptimizations, (int)nOptimizations);
        Assert.assertEquals("r.bytesSaved", expectedSavedBytes, r.bytesSaved);
    }
}
