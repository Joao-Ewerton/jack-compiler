import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;

    public CompilationEngine(JackTokenizer tokenizer, File outputFile) throws IOException {
        this.tokenizer = tokenizer;
        this.writer = new PrintWriter(outputFile);
        
        if (this.tokenizer.hasMoreTokens()) {
            this.tokenizer.advance();
        }
    }
    
    public void compileClass() {
        
        writer.close();
    }

    public void compileClassVarDec() {
    }

    public void compileSubroutine() {
    }

    public void compileParameterList() {
    }

    public void compileVarDec() {
    }

    
    //Comandos
    public void compileStatements() {
    }

    public void compileDo() {
    }

    public void compileLet() {
    }

    public void compileWhile() {
    }

    public void compileReturn() {
    }

    public void compileIf() {
    }

    
    //Expressões
    public void compileExpression() {
    }

    public void compileTerm() {
    }

    public void compileExpressionList() {
    }
}