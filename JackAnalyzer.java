import java.io.*;

public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) return;

        File fileOrDir = new File(args[0]);
        File[] files;

        if (fileOrDir.isFile()) files = new File[]{fileOrDir};
        else files = fileOrDir.listFiles((dir, name) -> name.endsWith(".jack"));

        if (files == null) return;

        for (File file : files) {
            try { processFile(file); } catch (Exception e) { }
        }
    }

    private static void processFile(File inputFile) throws IOException {
        String outputFilename = inputFile.getAbsolutePath().replace(".jack", "T.xml");
        PrintWriter writer = new PrintWriter(new File(outputFilename));
        JackTokenizer tokenizer = new JackTokenizer(inputFile);

        writer.println("<tokens>");
        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            String tag = tokenizer.tokenType().toString().toLowerCase();
            if (tag.equals("int_const")) tag = "integerConstant";
            if (tag.equals("string_const")) tag = "stringConstant";

            String val = tokenizer.getToken().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            writer.println("<" + tag + "> " + val + " </" + tag + ">");
        }
        writer.println("</tokens>");
        writer.close();
    }
}