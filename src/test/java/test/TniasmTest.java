/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import cl.MDLConfig;
import code.CodeBase;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.junit.Assert;
import org.junit.Test;
import util.Resources;
import workers.SourceCodeGenerator;

/**
 *
 * @author santi
 */
public class TniasmTest {

    private final MDLConfig config;
    private final CodeBase code;

    public TniasmTest() {
        config = new MDLConfig();
        code = new CodeBase(config);
    }

    @Test public void test1() throws IOException { Assert.assertTrue(test("data/generationtests/tniasm-ifdef.asm",
                                                                          "data/generationtests/tniasm-ifdef-expected.asm")); }
    @Test public void test2() throws IOException { Assert.assertTrue(test("data/generationtests/tniasm-constants.asm",
                                                                          "data/generationtests/tniasm-constants-expected.asm")); }
    @Test public void test3() throws IOException { Assert.assertTrue(test("data/generationtests/tniasm-multiple.asm",
                                                                          "data/generationtests/tniasm-multiple-expected.asm")); }
    @Test public void test4() throws IOException { Assert.assertTrue(test("data/generationtests/tniasm-macros.asm",
                                                                          "data/generationtests/tniasm-macros-expected.asm")); }
    @Test public void test5() throws IOException { Assert.assertTrue(test("data/generationtests/tniasm-error.asm",
                                                                          "data/generationtests/tniasm-error-expected.asm")); }

    private boolean test(String inputFile, String expectedOutputFile) throws IOException
    {
        Assert.assertTrue(config.parseArgs(inputFile,"-dialect","tniasm"));
        Assert.assertTrue(
                "Could not parse file " + inputFile,
                config.codeBaseParser.parseMainSourceFiles(config.inputFiles, code));

        SourceCodeGenerator scg = new SourceCodeGenerator(config);

        String result = scg.outputFileString(code.outputs.get(0), code);
        List<String> lines = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(result, "\n");
        while(st.hasMoreTokens()) {
            lines.add(st.nextToken().trim());
        }
        
        List<String> expectedLines = new ArrayList<>();
        if (expectedOutputFile != null) {
            BufferedReader br = Resources.asReader(expectedOutputFile);
            while(true) {
                String line = br.readLine();
                if (line == null) break;
                expectedLines.add(line.trim());
            }
        }
        System.out.println("\n--------------------------------------");
        System.out.println(result);
        System.out.println("--------------------------------------\n");
        
        for(int i = 0;i<Math.max(lines.size(), expectedLines.size());i++) {
            String line = lines.size() > i ? lines.get(i):"";
            String expectedLine = expectedLines.size() > i ? expectedLines.get(i):"";
            if (!line.equals(expectedLine)) {
                System.out.println("Line " + i + " was expected to be:\n" + expectedLine + "\nbut was:\n" + line);
                return false;
            }
        }
        
        return true;
    }    
}
