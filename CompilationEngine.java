import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;
    private int indentLevel = 0;

    public CompilationEngine(JackTokenizer tokenizer, File outputFile) throws IOException {
        this.tokenizer = tokenizer;
        this.writer = new PrintWriter(outputFile);
        if (this.tokenizer.hasMoreTokens()) {
            this.tokenizer.advance();
        }
    }

    private void writeIndented(String line) {
        for (int i = 0; i < indentLevel; i++) writer.print("  ");
        writer.println(line);
    }

    private void processToken() {
        String tag = tokenizer.tokenType().toString().toLowerCase();
        if (tag.equals("int_const")) tag = "integerConstant";
        if (tag.equals("string_const")) tag = "stringConstant";

        String val = tokenizer.getToken();
        val = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");

        writeIndented("<" + tag + "> " + val + " </" + tag + ">");
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }


    public void compileClass() {
        writeIndented("<class>");
        indentLevel++;

        processToken();
        processToken();
        processToken();

        while (tokenizer.tokenType() == TokenType.KEYWORD && 
              (tokenizer.getToken().equals("static") || tokenizer.getToken().equals("field"))) {
            compileClassVarDec();
        }

        while (tokenizer.tokenType() == TokenType.KEYWORD && 
              (tokenizer.getToken().equals("constructor") || tokenizer.getToken().equals("function") || tokenizer.getToken().equals("method"))) {
            compileSubroutine();
        }

        processToken();

        indentLevel--;
        writeIndented("</class>");
        writer.close();
    }

    public void compileClassVarDec() {
        writeIndented("<classVarDec>");
        indentLevel++;

        processToken();
        processToken();
        processToken();

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            processToken();
        }

        processToken();

        indentLevel--;
        writeIndented("</classVarDec>");
    }

    public void compileVarDec() {
        writeIndented("<varDec>");
        indentLevel++;

        processToken();
        processToken();
        processToken();

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            processToken();
        }

        processToken(); 

        indentLevel--;
        writeIndented("</varDec>");
    }

    public void compileSubroutine() {}
    public void compileParameterList() {}
    public void compileStatements() {}
    public void compileDo() {}
    public void compileLet() {}
    public void compileWhile() {}
    public void compileReturn() {}
    public void compileIf() {}
    public void compileExpression() {}
    public void compileTerm() {}
    public void compileExpressionList() {}
}