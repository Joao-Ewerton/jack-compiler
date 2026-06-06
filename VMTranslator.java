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
            System.out.println("Erro: Caminho inválido ou arquivo não é .vm");
        }
    }

    private static void processFile(File file) {
        String outputPath = file.getAbsolutePath().replace(".vm", ".asm");
        File outputFile = new File(outputPath);

        Parser parser = new Parser(file);
        CodeWriter codeWriter = new CodeWriter(outputFile);
        codeWriter.setFileName(file.getName());

        System.out.println("Processando: " + file.getName());

        while (parser.hasMoreCommands()) {
            parser.advance();
            int type = parser.commandType();

            if (type == Parser.C_ARITHMETIC) {
                codeWriter.writeArithmetic(parser.arg1());
            } else if (type == Parser.C_PUSH || type == Parser.C_POP) {
                codeWriter.writePushPop(type, parser.arg1(), parser.arg2());
            }
        }

        codeWriter.close();
        System.out.println("Arquivo convertido com sucesso: " + outputFile.getName());
    }
}