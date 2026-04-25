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
    
    public void compileStatements() {
        writeIndented("<statements>");
        indentLevel++;
        
        // roda enquanto achar um comando valido
        while (tokenizer.tokenType() == TokenType.KEYWORD && 
              (tokenizer.getToken().equals("let") || tokenizer.getToken().equals("if") || 
               tokenizer.getToken().equals("while") || tokenizer.getToken().equals("do") || 
               tokenizer.getToken().equals("return"))) {
            
            String token = tokenizer.getToken();
            if (token.equals("let")) {
                compileLet();
            } else if (token.equals("if")) {
                compileIf();
            } else if (token.equals("while")) {
                compileWhile();
            } else if (token.equals("do")) {
                compileDo();
            } else if (token.equals("return")) {
                compileReturn();
            }
        }
        
        indentLevel--;
        writeIndented("</statements>");
    }

    public void compileDo() {
        writeIndented("<doStatement>");
        indentLevel++;
        
        processToken(); // do
        processToken();
        
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(".")) {
            processToken();
            processToken(); // nome do metodo
        }
        
        processToken();
        compileExpressionList();
        processToken();
        processToken();
        
        indentLevel--;
        writeIndented("</doStatement>");
    }
    
    public void compileLet() {
        writeIndented("<letStatement>");
        indentLevel++;
        
        processToken();
        processToken();
        
        // checa se é array
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals("[")) {
            processToken();
            compileExpression();
            processToken();
        }
        
        processToken();
        compileExpression();
        processToken();
        
        indentLevel--;
        writeIndented("</letStatement>");
    }
    
    public void compileWhile() {
        writeIndented("<whileStatement>");
        indentLevel++;
        
        processToken();
        processToken();
        compileExpression();
        processToken();
        processToken();
        compileStatements();
        processToken();
        
        indentLevel--;
        writeIndented("</whileStatement>");
    }
    
    public void compileReturn() {
        writeIndented("<returnStatement>");
        indentLevel++;
        
        processToken();
        
        // se tiver algo alem do ; é pq tem expressão retornando
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(";"))) {
            compileExpression();
        }
        
        processToken();
        
        indentLevel--;
        writeIndented("</returnStatement>");
    }
    
    public void compileIf() {
        writeIndented("<ifStatement>");
        indentLevel++;
        
        processToken();
        processToken();
        compileExpression();
        processToken();
        processToken();
        compileStatements();
        processToken();
        
        // verifica se tem o else
        if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("else")) {
            processToken();
            processToken();
            compileStatements();
            processToken();
        }
        
        indentLevel--;
        writeIndented("</ifStatement>");
    }
    
    public void compileExpression() {}
    
    public void compileTerm() {}
    public void compileExpressionList() {}
}