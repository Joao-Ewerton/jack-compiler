import java.io.*;

public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java JackAnalyzer <arquivo.jack>");
            return;
        }

        try {
            File inputFile = new File(args[0]);
            String outputFilename = inputFile.getAbsolutePath().replace(".jack", "T.xml");
            PrintWriter writer = new PrintWriter(new File(outputFilename));
            JackTokenizer tokenizer = new JackTokenizer(inputFile);

            writer.println("<tokens>");
            while (tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                String tag = tokenizer.tokenType().toString().toLowerCase();
                if (tag.equals("int_const")) tag = "integerConstant";
                if (tag.equals("string_const")) tag = "stringConstant";

                writer.println("<" + tag + "> " + tokenizer.getToken() + " </" + tag + ">");
            }
            writer.println("</tokens>");
            writer.close();
            System.out.println("Gerado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}