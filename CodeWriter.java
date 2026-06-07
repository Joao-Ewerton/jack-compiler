import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String fileName;

    public CodeWriter(File file) {
        try { out = new PrintWriter(file); } 
        catch (FileNotFoundException e) {}
    }

    public void setFileName(String fileName) {
        this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public void writeArithmetic(String command) {
        out.println("// " + command);
        if (command.equals("add") || command.equals("sub")) {
            out.println("@SP\nAM=M-1\nD=M\nA=A-1"); // Desempilha 2 valores
            if (command.equals("add")) out.println("M=M+D");
            if (command.equals("sub")) out.println("M=M-D");
        }
    }

    public void writePushPop(int command, String segment, int index) {
        out.println("// push " + segment + " " + index);
        if (command == Parser.C_PUSH && segment.equals("constant")) {
            out.println("@" + index);
            out.println("D=A");
            // Coloca na pilha
            out.println("@SP\nA=M\nM=D\n@SP\nM=M+1");
        }
    }

    public void close() {
        if (out != null) out.close();
    }
}