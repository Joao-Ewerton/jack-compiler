import java.io.File;

public class VMTranslator {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java VMTranslator <arquivo.vm ou diretorio>");
            return;
        }

        File inputFile = new File(args[0]);
        
        if (inputFile.isFile() && inputFile.getName().endsWith(".vm")) {
            processFile(inputFile);
        } else if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles((dir, name) -> name.endsWith(".vm"));
            if (files != null) {
                for (File file : files) {
                    processFile(file);
                }
            }
        } else {
            System.out.println("Caminho inválido.");
        }
    }

    private static void processFile(File file) {
        String outputPath = file.getAbsolutePath().replace(".vm", ".asm");
        Parser parser = new Parser(file);
        CodeWriter codeWriter = new CodeWriter(new File(outputPath));
        codeWriter.setFileName(file.getName());

        System.out.println("Traduzindo: " + file.getName());

        while (parser.hasMoreCommands()) {
            parser.advance();
            int type = parser.commandType();

            if (type == Parser.C_ARITHMETIC) {
                codeWriter.writeArithmetic(parser.arg1());
            } else if (type == Parser.C_PUSH || type == Parser.C_POP) {
                codeWriter.writePushPop(type, parser.arg1(), parser.arg2());
            } 
            // Novos roteamentos - Controle de Fluxo
            else if (type == Parser.C_LABEL) {
                codeWriter.writeLabel(parser.arg1());
            } else if (type == Parser.C_GOTO) {
                codeWriter.writeGoto(parser.arg1());
            } else if (type == Parser.C_IF) {
                codeWriter.writeIf(parser.arg1());
            }
            //Novos roteamentos - Sub-rotinas
            else if (type == Parser.C_FUNCTION) {
                codeWriter.writeFunction(parser.arg1(), parser.arg2());
            } else if (type == Parser.C_CALL) {
                codeWriter.writeCall(parser.arg1(), parser.arg2());
            } else if (type == Parser.C_RETURN) {
                codeWriter.writeReturn();
            }
        }
        codeWriter.close();
    }
}