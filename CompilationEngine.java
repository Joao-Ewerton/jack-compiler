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
        symbolTable.startSubroutine();
        ifLabelNum = 0;
        whileLabelNum = 0;

        String subroutineType = tokenizer.getToken();
        processToken(); 
        
        processToken();
        String subName = tokenizer.getToken();
        processToken(); 

        if (subroutineType.equals("method")) {
            symbolTable.define("this", className, SymbolTable.Kind.ARG);
        }

        processToken();
        compileParameterList();
        processToken();
        processToken();
        
        while (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("var")) {
            compileVarDec();
        }
        
        int nLocals = symbolTable.varCount(SymbolTable.Kind.VAR);
        vmWriter.writeFunction(className + "." + subName, nLocals);
        
        if (subroutineType.equals("constructor")) {
            vmWriter.writePush(VMWriter.Segment.CONST, symbolTable.varCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (subroutineType.equals("method")) {
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
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
        String name = tokenizer.getToken();
        processToken();
        
        int nArgs = 0;
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(".")) {
            processToken();
            String subName = tokenizer.getToken();
            processToken();
            
            SymbolTable.Symbol sym = symbolTable.resolve(name);
            if (sym != null) {
                vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                name = sym.type() + "." + subName;
                nArgs++;
            } else {
                name = name + "." + subName;
            }
        } else {
            vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            name = className + "." + name;
            nArgs++;
        }
        
        processToken();
        nArgs += compileExpressionList();
        processToken();
        processToken();
        
        vmWriter.writeCall(name, nArgs);
        vmWriter.writePop(VMWriter.Segment.TEMP, 0);
    }
    
    public void compileLet() {
        processToken();
        String varName = tokenizer.getToken();
        processToken();
        
        boolean isArray = false;
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals("[")) {
            isArray = true;
            SymbolTable.Symbol sym = symbolTable.resolve(varName);
            vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
            
            processToken();
            compileExpression();
            processToken();
            
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }
        
        processToken();
        compileExpression();
        processToken();
        
        if (isArray) {
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 1);
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            SymbolTable.Symbol sym = symbolTable.resolve(varName);
            if (sym != null) {
                vmWriter.writePop(kindToSegment(sym.kind()), sym.index());
            }
        }
    }
    
    public void compileWhile() {
        String l1 = "WHILE_EXP" + whileLabelNum;
        String l2 = "WHILE_END" + whileLabelNum;
        whileLabelNum++;

        vmWriter.writeLabel(l1);
        
        processToken();
        processToken();
        compileExpression();
        processToken();
        
        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(l2);
        
        processToken();
        compileStatements();
        processToken();
        
        vmWriter.writeGoto(l1);
        vmWriter.writeLabel(l2);
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
        String l1 = "IF_TRUE" + ifLabelNum;
        String l2 = "IF_FALSE" + ifLabelNum;
        String l3 = "IF_END" + ifLabelNum;
        ifLabelNum++;

        processToken();
        processToken();
        compileExpression();
        processToken();
        
        vmWriter.writeIf(l1);
        vmWriter.writeGoto(l2);
        vmWriter.writeLabel(l1);
        
        processToken();
        compileStatements();
        processToken();
        
        if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.getToken().equals("else")) {
            vmWriter.writeGoto(l3);
            vmWriter.writeLabel(l2);
            
            processToken();
            processToken();
            compileStatements();
            processToken();
            
            vmWriter.writeLabel(l3);
        } else {
            vmWriter.writeLabel(l2);
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
            String name = val;
            processToken(); 
            
            if (tokenizer.tokenType() == TokenType.SYMBOL) {
                String nextToken = tokenizer.getToken();
                if (nextToken.equals("[")) {
                    SymbolTable.Symbol sym = symbolTable.resolve(name);
                    vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                    
                    processToken();
                    compileExpression();
                    processToken();
                    
                    vmWriter.writeArithmetic(VMWriter.Command.ADD);
                    vmWriter.writePop(VMWriter.Segment.POINTER, 1);
                    vmWriter.writePush(VMWriter.Segment.THAT, 0);
                } 
                else if (nextToken.equals("(") || nextToken.equals(".")) {
                    int nArgs = 0;
                    if (nextToken.equals(".")) {
                        processToken();
                        String subName = tokenizer.getToken();
                        processToken();
                        
                        SymbolTable.Symbol sym = symbolTable.resolve(name);
                        if (sym != null) {
                            vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                            name = sym.type() + "." + subName;
                            nArgs++;
                        } else {
                            name = name + "." + subName;
                        }
                    } else {
                        vmWriter.writePush(VMWriter.Segment.POINTER, 0);
                        name = className + "." + name;
                        nArgs++;
                    }
                    
                    processToken();
                    nArgs += compileExpressionList();
                    processToken();
                    
                    vmWriter.writeCall(name, nArgs);
                }
                else {
                    SymbolTable.Symbol sym = symbolTable.resolve(name);
                    if (sym != null) vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                }
            } else {
                SymbolTable.Symbol sym = symbolTable.resolve(name);
                if (sym != null) vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
            }
        }
    }

    public int compileExpressionList() {
        int nArgs = 0;
        if (!(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(")"))) {
            compileExpression();
            nArgs++;
            while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.getToken().equals(",")) {
                processToken();
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
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