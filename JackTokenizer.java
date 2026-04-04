import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JackTokenizer {
    private String content;
    private List<String> tokens;
    private int currentTokenIndex;
    private String currentToken;

    public JackTokenizer(File inputFile) throws IOException {
        content = readFile(inputFile);
        cleanContent();
        tokenize();
        currentTokenIndex = 0;
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

    private void cleanContent() {
        content = content.replaceAll("//.*", "");
        content = content.replaceAll("/\\*([\\s\\S]*?)\\*/", "");
    }

    // separar os tokens
    private void tokenize() {
        tokens = new ArrayList<>();
        String regex = "\"([^\"]*)\"|([\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*/&\\|<>=~])|(\\d+)|([a-zA-Z_]\\w*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
    }

    public boolean hasMoreTokens() {
        return currentTokenIndex < tokens.size();
    }

    public void advance() {
        if (hasMoreTokens()) {
            currentToken = tokens.get(currentTokenIndex);
            currentTokenIndex++;
        }
    }
}