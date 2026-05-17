package br.ufma.ecp;

import br.ufma.ecp.token.TokenType;
import java.nio.charset.StandardCharsets;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    
    private String className;
    private String subroutineName;
    private int ifLabelNum;
    private int whileLabelNum;

    public CompilationEngine(byte[] input) {
        String code = new String(input, StandardCharsets.UTF_8);
        this.tokenizer = new JackTokenizer(code);
        tokenizer.advance();
        
        this.vmWriter = new VMWriter();
        this.symbolTable = new SymbolTable();
        
        this.ifLabelNum = 0;
        this.whileLabelNum = 0;
    }

    public String getVMOutput() {
        return vmWriter.vmOutput();
    }

    public void parse() {
        compileClass();
    }

    private void processToken() {
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    public void compileClass() {
        processToken();
        className = tokenizer.getToken();
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
    }

    public void compileClassVarDec() {
        processToken();
        processToken();
        processToken();

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            processToken();
        }
        processToken();
    }

    public void compileVarDec() {
        processToken();
        processToken();
        processToken();

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            processToken();
        }
        processToken();
    }

    public void compileSubroutine() {
        processToken();
        processToken();
        processToken();
        processToken();
        compileParameterList();
        processToken();

        processToken();
        while (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("var")) {
            compileVarDec();
        }
        compileStatements();
        processToken();
    }

    public void compileParameterList() {
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            processToken();
            processToken();

            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken();
                processToken();
                processToken();
            }
        }
    }
    
    public void compileStatements() {
        while (tokenizer.tokenType() == TokenType.KEYWORD && 
              (tokenizer.getToken().equals("let") || tokenizer.getToken().equals("if") || 
               tokenizer.getToken().equals("while") || tokenizer.getToken().equals("do") || 
               tokenizer.getToken().equals("return"))) {
            
            String token = tokenizer.getToken();
            if (token.equals("let")) compileLet();
            else if (token.equals("if")) compileIf();
            else if (token.equals("while")) compileWhile();
            else if (token.equals("do")) compileDo();
            else if (token.equals("return")) compileReturn();
        }
    }

    public void compileDo() {
        processToken();
        processToken();
        
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(".")) {
            processToken();
            processToken();
        }
        
        processToken();
        compileExpressionList();
        processToken();
        processToken();
    }
    
    public void compileLet() {
        processToken();
        processToken();
        
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals("[")) {
            processToken();
            compileExpression();
            processToken();
        }
        
        processToken();
        compileExpression();
        processToken();
    }
    
    public void compileWhile() {
        processToken();
        processToken();
        compileExpression();
        processToken();
        processToken();
        compileStatements();
        processToken();
    }
    
    public void compileReturn() {
        processToken();
        
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(";"))) {
            compileExpression();
        }
        processToken();
    }
    
    public void compileIf() {
        processToken();
        processToken();
        compileExpression();
        processToken();
        processToken();
        compileStatements();
        processToken();
        
        if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("else")) {
            processToken();
            processToken();
            compileStatements();
            processToken();
        }
    }
    
    public void compileExpression() {
        compileTerm();

        while (tokenizer.tokenType() == TokenType.SYMBOL && isOp(tokenizer.getToken())) {
            String op = tokenizer.getToken();
            processToken();
            
            compileTerm();
            
            if (op.equals("+")) vmWriter.writeArithmetic(VMWriter.Command.ADD);
            else if (op.equals("-")) vmWriter.writeArithmetic(VMWriter.Command.SUB);
            else if (op.equals("*")) vmWriter.writeCall("Math.multiply", 2);
            else if (op.equals("/")) vmWriter.writeCall("Math.divide", 2);
            else if (op.equals("&")) vmWriter.writeArithmetic(VMWriter.Command.AND);
            else if (op.equals("|")) vmWriter.writeArithmetic(VMWriter.Command.OR);
            else if (op.equals("<")) vmWriter.writeArithmetic(VMWriter.Command.LT);
            else if (op.equals(">")) vmWriter.writeArithmetic(VMWriter.Command.GT);
            else if (op.equals("=")) vmWriter.writeArithmetic(VMWriter.Command.EQ);
        }
    }
    
    public void compileTerm() {
        TokenType type = tokenizer.tokenType();
        String val = tokenizer.getToken();

        if (type == TokenType.INT_CONST) {
            vmWriter.writePush(VMWriter.Segment.CONST, Integer.parseInt(val));
            processToken();
        } 
        else if (type == TokenType.STRING_CONST || 
           (type == TokenType.KEYWORD && (val.equals("true") || val.equals("false") || val.equals("null") || val.equals("this")))) {
            processToken();
        } 
        else if (type == TokenType.SYMBOL && (val.equals("-") || val.equals("~"))) {
            String op = val;
            processToken();
            compileTerm();
            
            if (op.equals("-")) vmWriter.writeArithmetic(VMWriter.Command.NEG);
            else if (op.equals("~")) vmWriter.writeArithmetic(VMWriter.Command.NOT);
        } 
        else if (type == TokenType.SYMBOL && val.equals("(")) {
            processToken();
            compileExpression();
            processToken();
        }
        else if (type == TokenType.IDENTIFIER) {
            processToken(); 
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
    }

    public void compileExpressionList() {
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            compileExpression();
            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken(); 
                compileExpression();
            }
        }
    }

    private boolean isOp(String c) {
        return c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/") ||
               c.equals("&") || c.equals("|") || c.equals("<") || c.equals(">") || c.equals("=");
    }
}