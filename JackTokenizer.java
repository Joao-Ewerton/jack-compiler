import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JackTokenizer {
    private String content;
    private List<String> tokens;
    private int currentTokenIndex;
    private String currentToken;
    private static final String SYMBOLS = "{}()[].,;+-*/&|<>=~";
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void", "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    ));

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

    public TokenType tokenType() {
        if (KEYWORDS.contains(currentToken)) return TokenType.KEYWORD;
        if (SYMBOLS.contains(currentToken)) return TokenType.SYMBOL;
        if (currentToken.startsWith("\"")) return TokenType.STRING_CONST;
        if (currentToken.matches("\\d+")) return TokenType.INT_CONST;
        return TokenType.IDENTIFIER;
    }

    public String getToken() {
        if (tokenType() == TokenType.STRING_CONST) {
            return currentToken.substring(1, currentToken.length() - 1);
        }
        return currentToken;
    }
}