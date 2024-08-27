/*
 * Author: Santiago Ontañón Villar (Brain Games)
 */
package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import cl.MDLConfig;
import code.CPUOpSpec;
import code.CPUOpSpecArg;
import util.Resources;

/**
 *
 * @author santi
 */
public class CPUOpSpecParser {
    MDLConfig config;

    public CPUOpSpecParser(MDLConfig a_config) {
        config = a_config;
    }

    public List<CPUOpSpec> parseSpecs() throws IOException
    {
        try (BufferedReader br = Resources.asReader(config.cpuInstructionSet)) {
            List<CPUOpSpec> specs = br.lines()
                    .filter(line -> !config.tokenizer.isSingleLineComment(line))
                    .map(line -> parseOpSpecLine(line.split("\t"), config))
                    .filter(opSpec -> opSpec != null)
                    .collect(Collectors.toList());
            
            for(CPUOpSpec spec:specs) {
                if (!spec.searchOfficialEquivalent(specs)) {
                    config.error("CPU op " + spec + " defined as unofficial, but there is no official equivalent!");
                    return null;
                }
            }
            
            return specs;
        }
    }


    public CPUOpSpec parseOpSpecLine(String data[], MDLConfig config)
    {
        int instructionColumn = 0;
        int timeColumn = 1;
        int byteRepresentationColumn = 2;
        int sizeColumn = 3;
        int inputColumn = 4;
        int outputColumn = 8;
        int officialColumn = 12;

        String op = data[instructionColumn];
        StringTokenizer st = new StringTokenizer(op, " ,");
        String opName = st.nextToken();
        int size = Integer.parseInt(data[sizeColumn]);
        String timesStr[] = data[timeColumn].split("/");
        if (timesStr[0].length() == 0) return null;
        int times[] = new int[timesStr.length];
        for(int i = 0;i<timesStr.length;i++) {
            times[i] = Integer.parseInt(timesStr[i]);
        }

        boolean official = true;
        if (data.length > officialColumn) {
            official = Boolean.parseBoolean(data[officialColumn]);
        }
        CPUOpSpec spec = new CPUOpSpec(opName.toLowerCase(), 
                size, times, data[byteRepresentationColumn], official, config);

        while(st.hasMoreTokens()) {
            String argStr = st.nextToken();
            CPUOpSpecArg arg = new CPUOpSpecArg();

            if (argStr.equals("C")) {
                if (spec.getName().equals("jr") ||
                    spec.getName().equals("jp") ||
                    spec.getName().equals("ret") ||
                    spec.getName().equals("call")) {
                    arg.condition = argStr;
                } else {
                    arg.reg = argStr;
                }
            } else  if (argStr.equals("A") ||
                argStr.equals("F") ||
                argStr.equals("B") ||
                argStr.equals("D") ||
                argStr.equals("E") ||
                argStr.equals("H") ||
                argStr.equals("L") ||
                argStr.equals("I") ||
                argStr.equals("R") ||
                argStr.equals("IXL") ||
                argStr.equals("IXH") ||
                argStr.equals("IYL") ||
                argStr.equals("IYH") ||
                argStr.equals("AF") ||
                argStr.equals("AF'") ||
                argStr.equals("BC") ||
                argStr.equals("DE") ||
                argStr.equals("HL") ||
                argStr.equals("IX") ||
                argStr.equals("IY") ||
                argStr.equals("SP")) {
                arg.reg = argStr.toUpperCase();

            } else if(argStr.equals("r") ||
                argStr.equals("p") ||
                argStr.equals("q") ||
                argStr.equals("IXp") ||
                argStr.equals("IYq")) {
                // we don't want to have these in upper case, to distinguish "r" from "R":
                arg.reg = argStr;

            } else if (argStr.equals("(C)")) {
                arg.regIndirection = "C";
            } else if (argStr.equals("(BC)")) {
                arg.regIndirection = "BC";
            } else if (argStr.equals("(DE)")) {
                arg.regIndirection = "DE";
            } else if (argStr.equals("(HL)")) {
                arg.regIndirection = "HL";
                if (opName.equalsIgnoreCase("jp")) spec.isJpRegWithParenthesis = true;
            } else if (argStr.equals("(SP)")) {
                arg.regIndirection = "SP";
            } else if (argStr.equals("(IX)")) {
                arg.regIndirection = "IX";
                if (opName.equalsIgnoreCase("jp")) spec.isJpRegWithParenthesis = true;
            } else if (argStr.equals("(IY)")) {
                arg.regIndirection = "IY";
                if (opName.equalsIgnoreCase("jp")) spec.isJpRegWithParenthesis = true;

            } else if (argStr.equals("(IX+o)")) {
                arg.regOffsetIndirection = "IX";
            } else if (argStr.equals("(IY+o)")) {
                arg.regOffsetIndirection = "IY";

            } else if (argStr.equals("NC") ||
                       argStr.equals("Z") ||
                       argStr.equals("NZ") ||
                       argStr.equals("P") ||
                       argStr.equals("M") ||
                       argStr.equals("PE") ||
                       argStr.equals("PO")) {
                arg.condition = argStr;

            } else if (argStr.equals("0")) {
                arg.byteConstantAllowed = true;
                arg.min = 0;
                arg.max = 0;
            } else if (argStr.equals("1")) {
                arg.byteConstantAllowed = true;
                arg.min = 1;
                arg.max = 1;
            } else if (argStr.equals("2")) {
                arg.byteConstantAllowed = true;
                arg.min = 2;
                arg.max = 2;
            } else if (argStr.equals("8H")) {
                arg.byteConstantAllowed = true;
                arg.min = 8;
                arg.max = 8;
            } else if (argStr.equals("10H")) {
                arg.byteConstantAllowed = true;
                arg.min = 16;
                arg.max = 16;
            } else if (argStr.equals("18H")) {
                arg.byteConstantAllowed = true;
                arg.min = 24;
                arg.max = 24;
            } else if (argStr.equals("20H")) {
                arg.byteConstantAllowed = true;
                arg.min = 32;
                arg.max = 32;
            } else if (argStr.equals("28H")) {
                arg.byteConstantAllowed = true;
                arg.min = 40;
                arg.max = 40;
            } else if (argStr.equals("30H")) {
                arg.byteConstantAllowed = true;
                arg.min = 48;
                arg.max = 48;
            } else if (argStr.equals("38H")) {
                arg.byteConstantAllowed = true;
                arg.min = 56;
                arg.max = 56;
            } else if (argStr.equals("b")) {
                arg.byteConstantAllowed = true;
                arg.min = 0;
                arg.max = 7;
            } else if (argStr.equals("n")) {
                arg.byteConstantAllowed = true;
            } else if (argStr.equals("n1")) {
                arg.byteConstantAllowed = true;
            } else if (argStr.equals("n2")) {
                arg.byteConstantAllowed = true;
            } else if (argStr.equals("nn")) {
                arg.wordConstantAllowed = true;
            } else if (argStr.equals("mm")) {
                arg.wordConstantBigEndianAllowed = true;
            } else if (argStr.equals("o")) {
                arg.relativeLabelAllowed = true;
            } else if (argStr.equals("(n)")) {
                arg.byteConstantIndirectionAllowed = true;
            } else if (argStr.equals("(nn)")) {
                arg.wordConstantIndirectionAllowed = true;
            } else {
                throw new IllegalArgumentException("unsupported argument " + argStr + " in " + op);
            }

            spec.addArgSpec(arg);
        }

        // parseArgs dependencies:
        // input:
        if (data.length>inputColumn) {
            spec.inputRegs = parseDeps(data[inputColumn]);
        } else {
            spec.inputRegs = new ArrayList<>();
        }
        if (data.length>inputColumn+1) {
            spec.inputFlags = parseDeps(data[inputColumn+1]);
        } else {
            spec.inputFlags = new ArrayList<>();
        }
        if (data.length>inputColumn+2) {
            List<String> l = parseDeps(data[inputColumn+2]);
            if (!l.isEmpty()) {
                if (l.size() == 1) {
                    spec.inputPort = l.get(0);
                } else {
                    config.error("More than one port specified as input dependency in CPUOpSpec for "+op+"!");
                    return null;
                }
            }
        }
        if (data.length>inputColumn+3) {
            List<String> l = parseDeps(data[inputColumn+3]);
            if (!l.isEmpty()) {
                switch (l.size()) {
                    case 1:
                        spec.inputMemoryStart = spec.inputMemoryEnd = l.get(0);
                        break;
                    case 2:
                        spec.inputMemoryStart = l.get(0);
                        spec.inputMemoryEnd = l.get(1);
                        break;
                    default:
                        config.error("Cannot parse input memory dependency for CPUOpSpec: "+data[inputColumn+3]+"!");
                        return null;
                }
            }
        }
        // output:
        if (data.length>outputColumn) {
            spec.outputRegs = parseDeps(data[outputColumn]);
        } else {
            spec.outputRegs = new ArrayList<>();
        }
        if (data.length>outputColumn+1) {
            spec.outputFlags = parseDeps(data[outputColumn+1]);
        } else {
            spec.outputFlags = new ArrayList<>();
        }
        if (data.length>outputColumn+2) {
            List<String> l = parseDeps(data[outputColumn+2]);
            if (!l.isEmpty()) {
                if (l.size() == 1) {
                    spec.outputPort = l.get(0);
                } else {
                    config.error("More than one port specified as output dependency in CPUOpSpec for "+op+"!");
                    return null;
                }
            }
        }
        if (data.length>outputColumn+3) {
            List<String> l = parseDeps(data[outputColumn+3]);
            if (!l.isEmpty()) {
                switch (l.size()) {
                    case 1:
                        spec.outputMemoryStart = spec.inputMemoryEnd = l.get(0);
                        break;
                    case 2:
                        spec.outputMemoryStart = l.get(0);
                        spec.outputMemoryEnd = l.get(1);
                        break;
                    default:
                        config.error("Cannot parse output memory dependency for CPUOpSpec: " + data[outputColumn+3] + "!");
                        return null;
                }
            }
        }
        
        spec.precomputeInstructionTypes();

        return spec;
    }


    public List<String> parseDeps(String regs_str)
    {
        List<String> regs = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(regs_str, ", ");

        while(st.hasMoreTokens()) {
            String reg = st.nextToken();
            regs.add(reg);
        }

        return regs;
    }

}
