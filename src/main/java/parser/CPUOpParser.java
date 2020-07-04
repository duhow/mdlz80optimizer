/*
 * Author: Santiago Ontañón Villar (Brain Games)
 */
package parser;

import cl.MDLConfig;
import code.CPUOp;
import code.CPUOpSpec;
import code.CPUOpSpecArg;
import code.CodeBase;
import code.Expression;
import code.SourceStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class CPUOpParser {
    MDLConfig config;

    List<CPUOpSpec> opSpecs;
    HashMap<String, List<CPUOpSpec>> opSpecHash = new HashMap<>();    
    
    
    public CPUOpParser(List<CPUOpSpec> a_opSpecs, MDLConfig a_config) {
        opSpecs = a_opSpecs;
        config = a_config;
        
        for(CPUOpSpec spec:opSpecs) {
            if (!opSpecHash.containsKey(spec.getName())) {
                opSpecHash.put(spec.getName(), new ArrayList<>());
            }
            opSpecHash.get(spec.getName()).add(spec);
        }        
    }
    
    
    public boolean isOpName(String name)
    {
        return opSpecHash.containsKey(name.toLowerCase());
    }
    
    
    public List<CPUOpSpec> getOpSpecs(String name)
    {
        List<CPUOpSpec> l = opSpecHash.get(name.toLowerCase());
        if (l != null) return l;
        return new ArrayList<>();
    }
    
    
    public CPUOp parseOp(String a_op, List<Expression> a_args, SourceStatement s, CodeBase code) 
    {
        CPUOp op;
        List<CPUOpSpec> candidates = getOpSpecs(a_op);
        for(CPUOpSpec opSpec:candidates) {
            op = parseOp(a_op, opSpec, a_args, s, code);
            if (op != null) return op;
        }
        if (candidates != null && !candidates.isEmpty()) {
            // try to see if any of the arguments was a constant with parenthesis that had been interpreted
            // as an indirection:
            boolean anyChange = false;
            for(int i = 0;i<a_args.size();i++) {
                Expression arg = a_args.get(i);
                if (arg.evaluatesToNumericConstant() && arg.type == Expression.EXPRESSION_PARENTHESIS) {
                    a_args.set(i, arg.args.get(0));
                    anyChange = true;
                }
            }
            if (anyChange) {
                // try again!
                for(CPUOpSpec opSpec:candidates) {
                    op = parseOp(a_op, opSpec, a_args, s, code);
                    if (op != null) return op;
                }
            }
        }
        
        return null;
    }
    
    
    public CPUOp parseOp(String a_op, CPUOpSpec spec, List<Expression> a_args, SourceStatement s, CodeBase code)
    {
        if (!a_op.equalsIgnoreCase(spec.opName)) return null;
        if (spec.args.size() != a_args.size()) return null;
        for(int i = 0; i<spec.args.size(); i++) {
            if (!spec.args.get(i).match(a_args.get(i), spec, s, code)) {
                return null;
            }
        }
        
        if (spec.isJpRegWithParenthesis && config.warningJpHlWithParenthesis) {
            config.warn("Use of confusing classic 'jp (hl)' syntax, rather than the more accurate 'jp hp' in " + 
                    s.source.fileName + ", " + s.lineNumber);
            config.annotation(s.source.fileName, s.lineNumber, "warning", "Use of confusing z80 'jp (reg)' syntax, rather than the more accurate 'jp reg'.");
        }

        if (!spec.official) {
            if (config.warningUnofficialOps) {
                config.warn("Unofficial op syntax used in " + s.source.fileName + ", " + s.lineNumber);
                config.annotation(s.source.fileName, s.lineNumber, "warning", "Unofficial op syntax.");
            }
            if (config.convertToOfficial) {
                return officialFromUnofficial(spec.officialEquivalent, spec, a_args, code);
            }
        }    
        return new CPUOp(spec, a_args, config);
    }        
    
    
    CPUOp officialFromUnofficial(CPUOpSpec officialSpec, CPUOpSpec unofficialSpec, List<Expression> a_args, CodeBase code)
    {
        List<Expression> officialArgs = new ArrayList<>();
        List<Integer> used = new ArrayList<>();
        for(CPUOpSpecArg officialArgSpec:officialSpec.args) {
            boolean found = false;
            for(int i = 0;i<unofficialSpec.args.size();i++) {
                if (used.contains(i)) continue;
                if (officialArgSpec.equals(unofficialSpec.args.get(i))) {
                    officialArgs.add(a_args.get(i));
                    used.add(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // if the missing argument is a register, that is precisely specified (in upper case), we fill it in:
                if (officialArgSpec.reg != null && 
                    officialArgSpec.reg.equals(officialArgSpec.reg.toUpperCase())) {
                    officialArgs.add(Expression.symbolExpression(officialArgSpec.reg.toLowerCase(), code, config));
                } else {
                    // case not supported:
                    config.error("Cannot turn unofficial assembler op to official!");
                    return null;
                }
            }
        }
        
        return new CPUOp(officialSpec, officialArgs, config);
    }
}
