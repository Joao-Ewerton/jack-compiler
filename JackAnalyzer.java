import java.io.*;

public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java JackAnalyzer <arquivo_ou_diretorio>");
            return;
        }

        File fileOrDir = new File(args[0]);
        File[] files;

        if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".jack")) {
            files = new File[]{fileOrDir};
        } else if (fileOrDir.isDirectory()) {
            files = fileOrDir.listFiles((dir, name) -> name.endsWith(".jack"));
        } else {
            System.out.println("Nenhum arquivo .jack encontrado.");
            return;
        }

        if (files != null) {
            for (File file : files) {
                try {
                    processFile(file);
                } catch (IOException e) {
                    System.out.println("Erro processando " + file.getName());
                }
            }
        }
    }

    private static void processFile(File inputFile) throws IOException {
        String outputFilename = inputFile.getAbsolutePath().replace(".jack", "T_Output.xml");
        PrintWriter writer = new PrintWriter(new File(outputFilename));
        String content = new String(java.nio.file.Files.readAllBytes(inputFile.toPath()));
        JackTokenizer tokenizer = new JackTokenizer(content);

        writer.println("<tokens>");
        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            String tag = tokenizer.tokenType().toString().toLowerCase();
            
            if (tag.equals("int_const")) tag = "integerConstant";
            if (tag.equals("string_const")) tag = "stringConstant";

            String val = tokenizer.getToken();
            
            val = val.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;");

            writer.println("<" + tag + "> " + val + " </" + tag + ">");
        }
        writer.println("</tokens>");
        writer.close();
        System.out.println("Criado: " + outputFilename);
    }
}