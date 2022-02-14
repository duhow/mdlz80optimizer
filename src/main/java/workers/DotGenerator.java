/*
 * author: Santiago Ontañón Villar (Brain Games)
 */
package workers;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import cl.MDLConfig;
import code.CodeBase;
import code.SourceFile;
import code.CodeStatement;

public class DotGenerator implements MDLWorker {
    public static final String BINARY_COLOR = "gray";

    MDLConfig config = null;
    String outputFileName = null;

    public DotGenerator(MDLConfig a_config)
    {
        config = a_config;
    }


    @Override
    public String docString()
    {
        // This string has MD tags, so that I can easily generate the corresponding documentation in github with the 
        // hidden "-helpmd" flag:        
        return "- ```-dot <output file>```: generates a dot file with a graph representing the whole source code. Convert it to a png using 'dot' like this: ```dot -Tpng <output file>.dot -o <output file>.png```\n";
    }


    @Override
    public String simpleDocString() {
        return "";
    }
    
    
    @Override
    public boolean parseFlag(List<String> flags)
    {
        if (flags.get(0).equals("-dot") && flags.size()>=2) {
            flags.remove(0);
            outputFileName = flags.remove(0);
            return true;
        }
        return false;
    }


    @Override
    public boolean work(CodeBase code)
    {
        if (outputFileName == null) return true;

        config.debug("Executing "+this.getClass().getSimpleName()+" worker...");

        HashMap<String, String> nodeNames = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        sb.append("digraph codeanalysis {\n");
        sb.append("graph[rankdir=LR];\n");

        // vertices:
        for(SourceFile f : code.getSourceFiles()) {
            String sName = "" + (nodeNames.size()+1);
            nodeNames.put(f.fileName, sName);

            sb.append(sName);
            sb.append(" [shape=record label=\"");
            sb.append(sourceFileDotCountent(f, code));
            sb.append("\"]\n");

            for(CodeStatement s : f.getStatements()) {
                if (config.includeBinariesInAnalysis) {
                    if (s.type == CodeStatement.STATEMENT_INCBIN) {
                        sName = "" + (nodeNames.size()+1);
                        nodeNames.put(s.incbin.getName(), sName);

                        sb.append(sName);
                        sb.append(" [style=filled fillcolor=");
                        sb.append(BINARY_COLOR);
                        sb.append(" shape=record label=\"{{name:|");
                        sb.append(s.incbin);
                        sb.append("}|{size:|");
                        sb.append(s.incbinSize);
                        sb.append("}}}");
                        sb.append("\"]\n");
                    }
                }
            }
        }

        // edges:
        for(SourceFile f: code.getSourceFiles()) {
            for(CodeStatement s : f.getStatements()) {
                if (s.type == CodeStatement.STATEMENT_INCLUDE) {
                    sb.append(nodeNames.get(f.fileName));
                    sb.append(" -> ");
                    sb.append(nodeNames.get(s.include.fileName));
                    sb.append("\n");
                } else if (s.type == CodeStatement.STATEMENT_INCBIN) {
                    if (config.includeBinariesInAnalysis) {
                        sb.append(nodeNames.get(f.fileName));
                        sb.append(" -> ");
                        sb.append(nodeNames.get(s.incbin.getName()));
                        sb.append("\n");
                    }
                }
            }
        }

        sb.append("}");

        try (FileWriter fw = new FileWriter(outputFileName)) {
            fw.write(sb.toString());
            fw.flush();
        } catch (Exception e) {
            config.error("Cannot write to file " + outputFileName);
            return false;
        }
        return true;
    }


    String sourceFileDotCountent(SourceFile f, CodeBase code)
    {
        String str = "{{";
        str += "{name:|" + f.fileName + "}|";
        str += "{size(self):|" + f.sizeInBytes(code, false, false, false) + "}|";
        str += "{size(total):|" + f.sizeInBytes(code, true, true, false) + "}|";
        str += "{"+config.timeUnit+"s(self):|" + f.accumTimingString() + "}";
        str += "}}";

        return str;
    }
    
    
    @Override
    public boolean triggered() {
        return outputFileName != null;
    }  
}
