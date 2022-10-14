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
import workers.searchopt.SearchBasedOptimizer;

/**
 *
 * @author santi
 */
public class SearchBasedGeneratorTest {
    private final MDLConfig config;
    private final CodeBase code;
    private final SearchBasedOptimizer sbo;
    
    public SearchBasedGeneratorTest() {
        config = new MDLConfig();
        sbo = new SearchBasedOptimizer(config);
        config.registerWorker(sbo);
        code = new CodeBase(config);
    }

    @Test public void test0() throws IOException { test("data/searchtests/test0.txt", "data/searchtests/test0-expected.asm"); }
    @Test public void test1() throws IOException { test("data/searchtests/test1.txt", "data/searchtests/test1-expected.asm"); }
    @Test public void test1b() throws IOException { test("data/searchtests/test1b.txt", new String[]{"data/searchtests/test1b-expected.asm", "data/searchtests/test1b-expected2.asm"}); }
    @Test public void testFlags1() throws IOException { test("data/searchtests/test-flags1.txt", "data/searchtests/test-flags1-expected.asm"); }

    // The rest of tests are disabled here by default, as initializing the Search-based optimizer is a bit slow
    // So, we don't want to do it every time MDL is re-build. Just make sure these tests are re-run before
    // any new release.
//    @Test public void test2() throws IOException { test("data/searchtests/test2.txt", "data/searchtests/test2-expected.asm"); }
//    @Test public void test2b() throws IOException { test("data/searchtests/test2b.txt", (String[])null); }
//    @Test public void test2c() throws IOException { test("data/searchtests/test2c.txt", "data/searchtests/test2c-expected.asm"); }
//    @Test public void test3() throws IOException { test("data/searchtests/test3.txt", "data/searchtests/test3-expected.asm"); }
//    @Test public void test4() throws IOException { test("data/searchtests/test4.txt", "data/searchtests/test4-expected.asm"); }
//    @Test public void test5() throws IOException { test("data/searchtests/test5.txt", "data/searchtests/test5-expected.asm"); }
//    @Test public void test5b() throws IOException { test("data/searchtests/test5b.txt", "data/searchtests/test5b-expected.asm"); }
//    @Test public void test6() throws IOException { test("data/searchtests/test6.txt", "data/searchtests/test6-expected.asm"); }
//    @Test public void test7() throws IOException { test("data/searchtests/test7.txt", "data/searchtests/test7-expected.asm"); }
    
//    @Test public void test8() throws IOException { test("data/searchtests/test8.txt", "data/searchtests/test8-expected.asm"); }
//    @Test public void test9() throws IOException { test("data/searchtests/test9.txt", "data/searchtests/test9-expected.asm"); }
//    @Test public void test10() throws IOException { test("data/searchtests/test10.txt", "data/searchtests/test10-expected.asm"); }
//    @Test public void test11() throws IOException { test("data/searchtests/test11.txt", "data/searchtests/test11-expected.asm"); }
//    @Test public void test12() throws IOException { test("data/searchtests/test12.txt", "data/searchtests/test12-expected.asm", "size"); }
//    @Test public void test13() throws IOException { test("data/searchtests/test13.txt", "data/searchtests/test13-expected.asm", "size"); }

//    @Test public void testLShift9() throws IOException { test("data/searchtests/test-large1.txt", "data/searchtests/test-large1-expected.asm"); }
//    @Test public void testLShift9size() throws IOException { test("data/searchtests/test-large1.txt", "data/searchtests/test-large1-size-expected.asm", "size"); }
//    @Test public void testLShift9time() throws IOException { test("data/searchtests/test-large1.txt", "data/searchtests/test-large1-time-expected.asm", "speed"); }

    // Current version: 0.06 sec (8947 solutions tested 1-thread)
//    @Test public void testLShift10() throws IOException { test("data/searchtests/test-large2.txt", "data/searchtests/test-large2-expected.asm"); }
//     Current version: 0.1 sec (2511 solutions tested 1-thread)
//    @Test public void testLShift10b() throws IOException { test("data/searchtests/test-large2b.txt", "data/searchtests/test-large2b-expected.asm"); }
    // Current version: 0.18 sec (10630 solutions tested 1-thread)
//    @Test public void testLShift10c() throws IOException { test("data/searchtests/test-large2c.txt", new String[]{"data/searchtests/test-large2c-expected.asm",
//                                                                                                                  "data/searchtests/test-large2c-expected2.asm"}); }
    // Current version: 0.24 sec (229810 solutions tested 1-thread)
//    @Test public void testLShift11() throws IOException { test("data/searchtests/test-large3.txt", "data/searchtests/test-large3-expected.asm"); }
//     Current version: 0.35 sec (441370 solutions tested 1-thread)
//    @Test public void testMin() throws IOException { test("data/searchtests/test-min.txt", "data/searchtests/test-min-expected.asm"); }
//     Current version: 0.60 sec (633452 solutions tested 1-thread)
//    @Test public void testSort() throws IOException { test("data/searchtests/test-sort.txt", "data/searchtests/test-sort-expected.asm"); }
    // Current version: 1.19 sec (5749967 solutions tested 1-thread)
//    @Test public void testLShift12() throws IOException { test("data/searchtests/test-large4.txt", "data/searchtests/test-large4-expected.asm"); }
//     Current version: 4.60 sec (119130116 solutions tested 8-thread)
//    @Test public void testLShift13() throws IOException { test("data/searchtests/test-large5.txt", "data/searchtests/test-large5-expected.asm"); }
    // Current version: 18.31 sec (496702600 solutions tested, 8-threads)
//    @Test public void testLShift13Hard() throws IOException { test("data/searchtests/test-large6.txt", "data/searchtests/test-large6-expected.asm"); }
    // Current version: 16.25 sec (155483907 solutions tested, 8-threads)
//    @Test public void testLShift13HardSpeed() throws IOException { test("data/searchtests/test-large6.txt", "data/searchtests/test-large6-time-expected.asm", "speed"); }
    // Current version: 8.85 sec (186384038 solutions tested, 8-threads)
//    @Test public void testLShift13HardSize() throws IOException { test("data/searchtests/test-large6.txt", "data/searchtests/test-large6-size-expected.asm", "size"); }

    // Current version: ???
//    @Test public void testSort2() throws IOException { test("data/searchtests/test-sort2.txt", "data/searchtests/test-sort2-expected.asm"); }
    
    // Current version: ???
//    @Test public void testLShift13Complete() throws IOException { test("data/searchtests/test-large7.txt", "data/searchtests/test-large7-expected.asm"); }

    
    private void test(String inputFile, String expectedOutput) throws IOException
    {
        test(inputFile, new String[]{expectedOutput}, null);
    }


    private void test(String inputFile, String expectedOutput[]) throws IOException
    {
        test(inputFile, expectedOutput, null);
    }
    

    private void test(String inputFile, String expectedOutput, String searchTypeArg) throws IOException
    {
        test(inputFile, new String[]{expectedOutput}, searchTypeArg);
    }
    
    
    private void test(String inputFile, String expectedOutputs[], String searchTypeArg) throws IOException
    {
        if (searchTypeArg != null) {
            Assert.assertTrue(config.parseArgs(inputFile, "-so", searchTypeArg));
        } else {
            Assert.assertTrue(config.parseArgs(inputFile, "-so"));
//            Assert.assertTrue(config.parseArgs(inputFile, "-so", "-so-threads", "1"));
        }
        if (expectedOutputs == null) {
            Assert.assertFalse(
                    "Solution found, when there should not have been one for specification file: " + inputFile,
                    sbo.work(code));
        } else {
            Assert.assertTrue(
                    "Could not generate code for specification file: " + inputFile,
                    sbo.work(code));
            // Compare standard assembler generation:
            SourceCodeGenerator scg = new SourceCodeGenerator(config);
            Assert.assertFalse(code.outputs.isEmpty());
            String result = scg.outputFileString(code.outputs.get(0), code);
            boolean matchesAtLeastOne = false;
            for(String expectedOutput:expectedOutputs) {
                if (compareOutputsWithAlternatives(result, expectedOutput)) {
                    matchesAtLeastOne = true;
                    break;
                }
            }
            Assert.assertTrue(matchesAtLeastOne);
        }
    }    
    
    
    public static boolean compareOutputsWithAlternatives(String result, String expectedOutputFile) throws IOException
    {
        List<String> lines = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(result, "\n");
        while(st.hasMoreTokens()) {
            lines.add(st.nextToken().trim());
        }
        
        List<List<String>> expectedAlternatives = new ArrayList<>();
        BufferedReader br = Resources.asReader(expectedOutputFile);
        List<String> expectedAlternative = new ArrayList<>();
        while(true) {
            String line = br.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.length() > 0) {
                if (line.contains("----")) {
                    expectedAlternatives.add(expectedAlternative);
                    expectedAlternative = new ArrayList<>();
                } else {
                    expectedAlternative.add(line);
                }
            }
        }
        if (!expectedAlternative.isEmpty() || expectedAlternatives.isEmpty()) {
            expectedAlternatives.add(expectedAlternative);
        }
        System.out.println("\n--------------------------------------");
        System.out.println(result);
        System.out.println("--------------------------------------\n");
        
        for(List<String> expected : expectedAlternatives) {
            boolean match = true;
            for(int i = 0;i<Math.max(lines.size(), expected.size());i++) {
                String line = lines.size() > i ? lines.get(i):"";
                String expectedLine = expected.size() > i ? expected.get(i):"";
                if (!line.equals(expectedLine)) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        
        return false;
    }       
}
