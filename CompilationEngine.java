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

    public void compileSubroutine() {
        writeIndented("<subroutineDec>");
        indentLevel++;

        processToken();
        processToken();
        processToken();
        processToken();
        
        compileParameterList();
        
        processToken();

        writeIndented("<subroutineBody>");
        indentLevel++;
        
        processToken();

        // pode haver várias declarações de variáveis locais
        while (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("var")) {
            compileVarDec();
        }

        compileStatements();

        processToken();
        
        indentLevel--;
        writeIndented("</subroutineBody>");

        indentLevel--;
        writeIndented("</subroutineDec>");
    }

    public void compileParameterList() {
        writeIndented("<parameterList>");
        indentLevel++;

        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            processToken();
            processToken();

            // se tiver mais parâmetros separados por vírgula
            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken();
                processToken();
                processToken();
            }
        }

        indentLevel--;
        writeIndented("</parameterList>");
    }
    
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
    
    public void compileExpression() {
        writeIndented("<expression>");
        indentLevel++;

        compileTerm();

        // vai rodar enquanto achar um op matemático ou lógico (+, -, *, etc).
        while (tokenizer.tokenType() == TokenType.SYMBOL && isOp(tokenizer.getToken())) {
            processToken(); // processa o operador
            compileTerm();
        }

        indentLevel--;
        writeIndented("</expression>");
    }
    
    public void compileTerm() {
        writeIndented("<term>");
        indentLevel++;

        TokenType type = tokenizer.tokenType();
        String val = tokenizer.getToken();

        // constantes numéricas, strings ou palavras-chave (true, false, null, this)
        if (type == TokenType.INT_CONST || type == TokenType.STRING_CONST || 
           (type == TokenType.KEYWORD && (val.equals("true") || val.equals("false") || val.equals("null") || val.equals("this")))) {
            processToken();
        } 
        // operadores unários (-x, ~y)
        else if (type == TokenType.SYMBOL && (val.equals("-") || val.equals("~"))) {
            processToken();
            compileTerm();
        } 
        // expressões entre parênteses (x + y)
        else if (type == TokenType.SYMBOL && val.equals("(")) {
            processToken();
            compileExpression();
            processToken();
        } 
        else if (type == TokenType.IDENTIFIER) {
            processToken();
            
            // olha o próximo token para descobrir o que é
            if (tokenizer.tokenType() == TokenType.SYMBOL) {
                String nextToken = tokenizer.getToken();
                
                if (nextToken.equals("[")) {
                    processToken();
                    compileExpression();
                    processToken();
                } 
                else if (nextToken.equals("(")) {
                    processToken();
                    compileExpressionList();
                    processToken();
                } 
                else if (nextToken.equals(".")) {
                    processToken();
                    processToken();
                    processToken();
                    compileExpressionList();
                    processToken();
                }
            }
        }

        indentLevel--;
        writeIndented("</term>");
    }

    public void compileExpressionList() {
        writeIndented("<expressionList>");
        indentLevel++;

        // se o token não for ')', então tem pelo menos uma expressão aqui dentro
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            compileExpression();

            // pega as próximas expressões separadas por vírgula
            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken();
                compileExpression();
            }
        }

        indentLevel--;
        writeIndented("</expressionList>");
    }

    private boolean isOp(String c) {
        return c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/") ||
               c.equals("&") || c.equals("|") || c.equals("<") || c.equals(">") || c.equals("=");
    }
}