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
        SymbolTable.Kind kind = tokenizer.getToken().equals("static") ? SymbolTable.Kind.STATIC : SymbolTable.Kind.FIELD;
        processToken();
        String type = tokenizer.getToken();
        processToken();
        String name = tokenizer.getToken();
        processToken();
        symbolTable.define(name, type, kind);

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            name = tokenizer.getToken();
            processToken();
            symbolTable.define(name, type, kind);
        }
        processToken();
    }

    public void compileVarDec() {
        processToken();
        String type = tokenizer.getToken();
        processToken();
        String name = tokenizer.getToken();
        processToken();
        symbolTable.define(name, type, SymbolTable.Kind.VAR);

        while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
            processToken();
            name = tokenizer.getToken();
            processToken();
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
        }
        processToken();
    }

    public void compileParameterList() {
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            String type = tokenizer.getToken();
            processToken();
            String name = tokenizer.getToken();
            processToken();
            symbolTable.define(name, type, SymbolTable.Kind.ARG);

            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken();
                type = tokenizer.getToken();
                processToken();
                name = tokenizer.getToken();
                processToken();
                symbolTable.define(name, type, SymbolTable.Kind.ARG);
            }
        }
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
        String varName = tokenizer.getToken();
        processToken();
        
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals("[")) {
            processToken(); compileExpression(); processToken();
        }
        
        processToken();
        compileExpression();
        processToken();
        
        SymbolTable.Symbol sym = symbolTable.resolve(varName);
        if (sym != null) {
            vmWriter.writePop(kindToSegment(sym.kind()), sym.index());
        }
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
        } else {
            vmWriter.writePush(VMWriter.Segment.CONST, 0); 
        }
        
        processToken();
        vmWriter.writeReturn();
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

        // 1 - Constantes Inteiras
        if (type == TokenType.INT_CONST) {
            vmWriter.writePush(VMWriter.Segment.CONST, Integer.parseInt(val));
            processToken();
        } 
        // 2 - Constantes de String
        else if (type == TokenType.STRING_CONST) {
            String str = val;
            vmWriter.writePush(VMWriter.Segment.CONST, str.length());
            vmWriter.writeCall("String.new", 1);
            for (int i = 0; i < str.length(); i++) {
                vmWriter.writePush(VMWriter.Segment.CONST, (int) str.charAt(i));
                vmWriter.writeCall("String.appendChar", 2);
            }
            processToken();
        }
        // 3 - Palavras-chave (true, false, null, this)
        else if (type == TokenType.KEYWORD && (val.equals("true") || val.equals("false") || val.equals("null") || val.equals("this"))) {
            if (val.equals("false") || val.equals("null")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
            } else if (val.equals("true")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
                vmWriter.writeArithmetic(VMWriter.Command.NOT);
            } else if (val.equals("this")) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            }
            processToken();
        } 
        // 4 - Operadores Unários
        else if (type == TokenType.SYMBOL && (val.equals("-") || val.equals("~"))) {
            String op = val;
            processToken();
            compileTerm();
            
            if (op.equals("-")) vmWriter.writeArithmetic(VMWriter.Command.NEG);
            else if (op.equals("~")) vmWriter.writeArithmetic(VMWriter.Command.NOT);
        } 
        // 5 - Expressões entre parênteses (x + y)
        else if (type == TokenType.SYMBOL && val.equals("(")) {
            processToken();
            compileExpression();
            processToken();
        } 
        // 6 - Identificadores
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
                else {
                    SymbolTable.Symbol sym = symbolTable.resolve(name);
                    if (sym != null) {
                        vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                    }
                }
            } else {
                SymbolTable.Symbol sym = symbolTable.resolve(name);
                if (sym != null) {
                    vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
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

    private VMWriter.Segment kindToSegment(SymbolTable.Kind kind) {
        switch (kind) {
            case STATIC: return VMWriter.Segment.STATIC;
            case FIELD:  return VMWriter.Segment.THIS;
            case VAR:    return VMWriter.Segment.LOCAL;
            case ARG:    return VMWriter.Segment.ARG;
            default:     return VMWriter.Segment.CONST;
        }
    }

    private boolean isOp(String c) {
        return c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/") ||
               c.equals("&") || c.equals("|") || c.equals("<") || c.equals(">") || c.equals("=");
    }
}