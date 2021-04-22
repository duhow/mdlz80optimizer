/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workers.searchopt;

import cl.MDLConfig;
import code.CPUOp;
import code.CodeBase;
import code.CodeStatement;
import code.Expression;
import code.SourceFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import parser.SourceLine;
import util.microprocessor.IMemory;
import util.microprocessor.PlainZ80IO;
import util.microprocessor.PlainZ80Memory;
import util.microprocessor.Z80.CPUConfig;
import util.microprocessor.Z80.CPUConstants.RegisterNames;
import util.microprocessor.Z80.Z80Core;
import workers.MDLWorker;

/**
 *
 * @author santi
 */
public class SearchBasedOptimizer implements MDLWorker {
    MDLConfig config;
    boolean trigger = false;
    boolean showNewBestDuringSearch = true;
    
    int numberOfRandomSolutionChecks = 100;    
        
    public static class SolutionRecord {
        List<CPUOp> ops = new ArrayList<>();
        List<CPUOp> bestOps = null;
        int bestSize = 0;
        int bestTime = 0;
    }
    
    
    public SearchBasedOptimizer(MDLConfig a_config)
    {
        config = a_config;
    }
    
    
    @Override
    public String docString() {
        return "- ```-so```: Runs the search-based-based optimizer (input file is a function specification instead of an assembler file).\n";
    }

    
    @Override
    public String simpleDocString() {
        return "- ```-so```: Runs the search-based-based optimizer (input file is a function specification instead of an assembler file).\n";
    }

    
    @Override
    public boolean parseFlag(List<String> flags) {
        if (flags.get(0).equals("-so")) {
            flags.remove(0);
            trigger = true;
            config.codeSource = MDLConfig.CODE_FROM_SEARCHBASEDOPTIMIZER;
            return true;
        }
        return false;    
    }

    
    @Override
    public boolean triggered() {
        return trigger;
    }

    
    @Override
    public boolean work(CodeBase code) {
        // Parse specification file:
        Specification spec = SpecificationParser.parse(config.inputFile, code, config);
        if (spec == null) {
            config.error("Cannot parse the input specification file '"+config.inputFile+"'");
            return false;
        }
                
        SourceFile sf = new SourceFile("autogenerated.asm", null, null, code, config);
        code.addOutput(null, sf, 0);
        
        // Run the search process to generate code:
        List<SBOCandidate> candidateOps = precomputeCandidateOps(spec, code);
        if (candidateOps == null) return false;

        // Create a simulator:
        PlainZ80Memory z80Memory = new PlainZ80Memory();
        
        // Search via iterative deepening:
        int maxDepth = 2;
        SolutionRecord sr = new SolutionRecord();
        for(int depth = 1; depth<=maxDepth; depth++) {
            if (depthFirstSearch(depth, candidateOps, 
                                 spec, code, 
                                 spec.codeStartAddress, z80Memory,
                                 sr)) {
                // solution found!
                break;
            }
        }

        if (sr.bestOps == null) return false;
        
        int lineNumber = 1;
        for(CPUOp op:sr.bestOps) {
            SourceLine sl = new SourceLine("    " + op.toString(), sf, lineNumber);
            CodeStatement s = new CodeStatement(CodeStatement.STATEMENT_CPUOP, sl, sf, config);
            s.op = op;
            sf.getStatements().add(s);
            lineNumber++;
        }
                
        return true;
    }
    
    
    List<SBOCandidate> precomputeCandidateOps(Specification spec, CodeBase code)
    {
        List<SBOCandidate> candidates = new ArrayList<>();
        
        /*
        OK - AND/OR/XOR
        OK - INC/DEC
        OK - ADD/ADC/SUB/SBC
        ***- LD
        ***- RLCA/RLA/RRCA/RRA/RLC/RL/RRC/RR/SLA/SRA/SRL
        - PUSH/POP
        - EX/EXX
        - LDI/LDD/LDIR/LDDR
        - CPI/CPD/CPIR/CPDR
        - CP
        - DAA
        - CPL/NEG
        - CCF/SCF
        - HALT/DI/EI/IM
        - RLD/RRD
        - BIT/SET/RES
        - JP/JR/DJNZ
        - CALL/RET/RETI/RETN
        - RST
        - IN/INI/IND/INIR/INDR
        - OUT/OUTI/OUTD/OTIR/OTDR
        - NOP
        */
        
        if (spec.allowAndOrXorOps) {
            if (!precomputeAndOrXor(candidates, spec, code)) return null;
        }
        if (spec.allowIncDecOps) {
            if (!precomputeIncDec(candidates, spec, code)) return null;
        }
        if (spec.allowAddAdcSubSbc) {
            if (!precomputeAddAdcSubSbc(candidates, spec, code)) return null;
        }
        
        return candidates;
    }
    
    
    boolean precomputeOp(String line, List<SBOCandidate> candidates, CodeBase code)
    {
        List<String> tokens = config.tokenizer.tokenize(line);
        SourceFile sf = new SourceFile("dummy", null, null, code, config);
        SourceLine sl = new SourceLine(line, sf, 0);
        List<CodeStatement> l = config.lineParser.parse(tokens, sl, sf, null, code, config);
        if (l.size() != 1) {
            config.error("Parsing candidate op in the search-based optimizer resulted in more than one op!");
            return false;
        }
        CodeStatement s = l.get(0);
        SBOCandidate candidate = new SBOCandidate(s.op, code, config);
        if (candidate.bytes == null) return false;
        candidates.add(candidate);
        return true;
    }
    
    
    boolean precomputeAndOrXor(List<SBOCandidate> candidates, Specification spec, 
                               CodeBase code)
    {
        String opNames[] = {"and", "or", "xor"};
        String regNames[] = {"a", "b", "c", "d", "e", "h", "l"};
        for(String opName : opNames) {
            // register argument:
            for(String regName : regNames) {
                String line = opName + " " + regName;
                if (!precomputeOp(line, candidates, code)) return false;
            }
            
            // constant argument:
            // ...
            
            // (hl):
            {
                String line = opName + " (hl)";
                if (!precomputeOp(line, candidates, code)) return false;            
            }
            
            // (ix+d) / (iy+d):
            // ...
        }
        return true;
    }
    

    boolean precomputeIncDec(List<SBOCandidate> candidates, Specification spec, 
                               CodeBase code)
    {
        String opNames[] = {"inc", "dec"};
        String regNames[] = {"a", "b", "c", "d", "e", "h", "l",
                             "bc", "de", "hl", 
                             "ix", "iy",
                             "ixh", "ixl", "iyh", "iyl"};
        for(String opName : opNames) {
            // register argument:
            for(String regName : regNames) {
                String line = opName + " " + regName;
                if (!precomputeOp(line, candidates, code)) return false;
            }
            
            // (hl):
            {
                String line = opName + " (hl)";
                if (!precomputeOp(line, candidates, code)) return false;            
            }
            
            // (ix+d) / (iy+d):
            // ...
        }
        return true;
    }
    
    
    boolean precomputeAddAdcSubSbc(List<SBOCandidate> candidates, Specification spec, 
                               CodeBase code)
    {
        String opNames[] = {"add", "adc", "sub", "sbc"};
        String regNames[] = {"a", "b", "c", "d", "e", "h", "l", "ixh", "ixl", "iyh", "iyl"};
        for(String opName : opNames) {
            // register argument:
            for(String regName : regNames) {
                String line = opName + " a," + regName;
                if (opName.equals("sub") && regName.startsWith("i")) continue;
                if (!precomputeOp(line, candidates, code)) return false;
            }
            
            // constant argument:
            // ...
            
            // (hl):
            {
                String line = opName + " a,(hl)";
                if (!precomputeOp(line, candidates, code)) return false;            
            }
            
            // (ix+d) / (iy+d):
            // ...
        }
        
        String ops16bit[] = {"add hl,bc", "add hl,de", "add hl,hl", "add hl,sp",
                             "add ix,bc", "add ix,de", "add ix,ix", "add ix,sp",
                             "add iy,bc", "add iy,de", "add iy,iy", "add iy,sp",
        
                             "adc hl,bc", "adc hl,de", "adc hl,hl", "adc hl,sp",
                             "sbc hl,bc", "sbc hl,de", "sbc hl,hl", "sbc hl,sp"};
        for(String line:ops16bit) {
            if (!precomputeOp(line, candidates, code)) return false;            
        }
        return true;
    }    
        
    
    boolean depthFirstSearch(int depth, List<SBOCandidate> candidateOps, 
                             Specification spec, CodeBase code,
                             int codeAddress, IMemory z80Memory,
                             SolutionRecord sr)
    {
        if (depth == 0) {
            try {
                int time = 0;
                for(int i = 0; i < numberOfRandomSolutionChecks; i++) {
                    time = evaluateSolution(codeAddress, z80Memory, spec, code);
                    if (time < 0) return false;
                }
                int size = codeAddress - spec.codeStartAddress;
                if (sr.bestOps == null || 
                    size < sr.bestSize ||
                    (size == sr.bestSize && time < sr.bestTime)) {
                    sr.bestOps = new ArrayList<>();
                    sr.bestOps.addAll(sr.ops);
                    sr.bestSize = size;
                    sr.bestTime = time;
                    
                    if (showNewBestDuringSearch) {
                        config.info("New solution found (size: "+size+", time: " + time + "):");
                        for(CPUOp op:sr.bestOps) {
                            config.info("    " + op);
                        }
                    }
                }
                return true;
            }catch(Exception e) {
                config.error("Something went wrong during solution execution in the z80 simulator.");
                return false;
            }
        } else {
            boolean found = false;
            for(SBOCandidate candidate : candidateOps) {
                int nextAddress = codeAddress;
                for(int i = 0; i < candidate.bytes.length; i++) {
                    z80Memory.writeByte(nextAddress, candidate.bytes[i]);
                    nextAddress++;
                }
                sr.ops.add(candidate.op);
                if (depthFirstSearch(depth-1, candidateOps, spec, code, nextAddress, z80Memory, sr)) {
                    found = true;
                    // we keep going, in case we find a solution of the same size, but faster
                }
                sr.ops.remove(sr.ops.size()-1);
            }
            return found;
        }
    }
    
    
    // return -1 is solution fails
    // return time it takes if solution succeeds
    int evaluateSolution(int breakPoint, IMemory z80Memory, 
                         Specification spec, CodeBase code) throws Exception
    {
        // evaluate solution:
        Random rand = new Random();
        Z80Core z80 = new Z80Core(z80Memory, new PlainZ80IO(), new CPUConfig(config));
        z80.reset();

        // randomize the register contents:
        RegisterNames eightBitRegisters[] = {
            RegisterNames.A, RegisterNames.F,
            RegisterNames.B, RegisterNames.C,
            RegisterNames.D, RegisterNames.E,
            RegisterNames.H, RegisterNames.L,
            RegisterNames.A_ALT, RegisterNames.F_ALT,
            RegisterNames.B_ALT, RegisterNames.C_ALT,
            RegisterNames.D_ALT, RegisterNames.E_ALT,
            RegisterNames.H_ALT, RegisterNames.L_ALT,
            RegisterNames.IXH, RegisterNames.IXL,
            RegisterNames.IYH, RegisterNames.IYL,
            RegisterNames.R
        };
        for(RegisterNames register: eightBitRegisters) {
            z80.setRegisterValue(register, rand.nextInt(256));
        }
        z80.setProgramCounter(spec.codeStartAddress);
                         
        // randomize constants:
        for(InputParameter parameter:spec.parameters) {
            int value = parameter.minValue + rand.nextInt((parameter.maxValue - parameter.minValue)+1);
            parameter.symbol.exp = Expression.constantExpression(value, config);
            parameter.symbol.clearCache();            
        }
        
        // randomize the memory contents:
        // ...
        
        // execute initial state:
        if (!spec.initCPU(z80, code)) {
            return -1;
        }
        
        while(z80.getProgramCounter() < breakPoint && 
              z80.getTStates() < spec.maxSimulationTime) {
            z80.executeOneInstruction();
        }
        
        // check if the solution worked:
        if (spec.checkGoalState(z80, z80Memory, code)) {
            return (int)z80.getTStates();
        } else {
            return -1;
        }
    }
}
