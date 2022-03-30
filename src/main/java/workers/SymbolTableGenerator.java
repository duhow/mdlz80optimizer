/*
 * Author: Santiago Ontañón Villar (Brain Games)
 */
package workers;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cl.MDLConfig;
import code.CodeBase;
import code.Expression;
import code.SourceConstant;

/**
 *
 * @author santi
 */
public class SymbolTableGenerator implements MDLWorker {

    MDLConfig config = null;

    String outputFileName = null;
    public boolean includeConstants = false;

    public SymbolTableGenerator(MDLConfig a_config)
    {
        config = a_config;
    }
    

    @Override
    public String docString() {
        // This string has MD tags, so that I can easily generate the corresponding documentation in github with the 
        // hidden "-helpmd" flag:        
        return "- ```-st <output file>```: to output the symbol table.\n" +
               "- ```-st-constants```: includes constants, in addition to labels, in the output symbol table.\n";
    }

    
    @Override
    public String simpleDocString() {
        return "";
    }

    
    @Override
    public boolean parseFlag(List<String> flags) {
        if (flags.get(0).equals("-st") && flags.size()>=2) {
            flags.remove(0);
            outputFileName = flags.remove(0);
            return true;
        }
        if (flags.get(0).equals("-st-constants")) {
            flags.remove(0);
            includeConstants = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean work(CodeBase code) {

        if (outputFileName != null) {
            config.debug("Executing "+this.getClass().getSimpleName()+" worker...");

            try (FileWriter fw = new FileWriter(outputFileName)) {
                fw.write(symbolTableString(code));
                fw.flush();
            } catch (Exception e) {
                config.error("Cannot write to file " + outputFileName + ": " + e);
                return false;
            }
        }

        return true;
    }


    public String symbolTableString(CodeBase code)
    {
        List<String> sortedSymbols = new ArrayList<>();
        sortedSymbols.addAll(code.getSymbols());
        Collections.sort(sortedSymbols);
        StringBuilder sb = new StringBuilder();
        for(String name:sortedSymbols) {
            SourceConstant symbol = code.getSymbol(name);
            if (symbol.exp == null) continue;
            if (symbol.exp.type == Expression.EXPRESSION_SYMBOL &&
                symbol.exp.symbolName.equalsIgnoreCase(CodeBase.CURRENT_ADDRESS) ||
                includeConstants) {
                sb.append(name);
                sb.append(": equ ");
                if (symbol.exp.isConstant()) {
                    sb.append(symbol.getValue(code, true));
                } else if (symbol.isLabel()) {
                    Object value = symbol.getValue(code, true);
                    if (value instanceof Integer) {
                        int int_value = (Integer)value;
                        if (int_value < 0x10000) {
                            if (config.hexStyleChanged) {
                                sb.append(config.tokenizer.toHexWord(int_value, config.hexStyle));
                            } else {
                                sb.append(config.tokenizer.toHexWord(int_value, MDLConfig.HEX_STYLE_0X));
                            }
                        } else {
                            if (config.hexStyleChanged) {
                                sb.append(config.tokenizer.toHexDWord(int_value, config.hexStyle));
                            } else {
                                sb.append(config.tokenizer.toHexDWord(int_value, MDLConfig.HEX_STYLE_0X));
                            }
                        }
                    } else {
                        sb.append(value);
                    }
                } else {
                    sb.append(symbol.getValue(code, true));
                    sb.append("  ; ");
                    sb.append(symbol.exp);
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean triggered() {
        return outputFileName != null;
    }
}
