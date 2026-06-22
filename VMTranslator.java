import java.io.File;

public class VMTranslator {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java VMTranslator <arquivo.vm ou diretorio>");
            return;
        }

        File input = new File(args[0]);
        File outputFile;
        File[] vmFiles;

        // Verifica se a entrada é um diretório ou um arquivo único
        if (input.isDirectory()) {
            outputFile = new File(input, input.getName() + ".asm");
            vmFiles = input.listFiles((dir, name) -> name.endsWith(".vm"));
        } else {
            outputFile = new File(input.getAbsolutePath().replace(".vm", ".asm"));
            vmFiles = new File[]{input};
        }

        if (vmFiles == null || vmFiles.length == 0) {
            System.out.println("Nenhum arquivo .vm encontrado.");
            return;
        }

        CodeWriter codeWriter = new CodeWriter(outputFile);

        if (input.isDirectory()) {
            codeWriter.writeInit();
        }

        for (File vmFile : vmFiles) {
            System.out.println("Traduzindo: " + vmFile.getName());
            codeWriter.setFileName(vmFile.getName());
            Parser parser = new Parser(vmFile);

            while (parser.hasMoreCommands()) {
                parser.advance();
                int type = parser.commandType();

                if (type == Parser.C_ARITHMETIC) {
                    codeWriter.writeArithmetic(parser.arg1());
                } else if (type == Parser.C_PUSH || type == Parser.C_POP) {
                    codeWriter.writePushPop(type, parser.arg1(), parser.arg2());
                } else if (type == Parser.C_LABEL) {
                    codeWriter.writeLabel(parser.arg1());
                } else if (type == Parser.C_GOTO) {
                    codeWriter.writeGoto(parser.arg1());
                } else if (type == Parser.C_IF) {
                    codeWriter.writeIf(parser.arg1());
                } else if (type == Parser.C_FUNCTION) {
                    codeWriter.writeFunction(parser.arg1(), parser.arg2());
                } else if (type == Parser.C_CALL) {
                    codeWriter.writeCall(parser.arg1(), parser.arg2());
                } else if (type == Parser.C_RETURN) {
                    codeWriter.writeReturn();
                }
            }
        }
        
        codeWriter.close();
        System.out.println("Tradução concluída: " + outputFile.getAbsolutePath());
    }
}