import java.io.File;

public class VMTranslator {
    public static void main(String[] args) {
        if (args.length != 1) return;
        
        File inputFile = new File(args[0]);
        if (inputFile.isFile() && inputFile.getName().endsWith(".vm")) {
            processFile(inputFile);
        }
    }

    private static void processFile(File file) {
        String outputPath = file.getAbsolutePath().replace(".vm", ".asm");
        Parser parser = new Parser(file);
        CodeWriter codeWriter = new CodeWriter(new File(outputPath));
        codeWriter.setFileName(file.getName());

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
    }
}