import java.io.*;

public class JackTokenizer {
    private String content;

    public JackTokenizer(File inputFile) throws IOException {
        content = readFile(inputFile);
    }

    public String getContent() {
        return content;
    }

    private String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

}