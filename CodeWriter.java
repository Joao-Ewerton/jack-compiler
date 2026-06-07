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
            out.println("@SP\nAM=M-1\nD=M\nA=A-1");
            if (command.equals("add")) out.println("M=M+D");
            if (command.equals("sub")) out.println("M=M-D");
        }
    }

    public void writePushPop(int command, String segment, int index) {
        out.println("// " + (command == Parser.C_PUSH ? "push" : "pop") + " " + segment + " " + index);
        
        if (command == Parser.C_PUSH) {
            if (segment.equals("constant")) {
                out.println("@" + index + "\nD=A");
            } else if (isBaseSegment(segment)) {
                out.println("@" + getSegmentPointer(segment) + "\nD=M\n@" + index + "\nA=D+A\nD=M");
            } else if (segment.equals("temp")) {
                out.println("@" + (5 + index) + "\nD=M");
            } else if (segment.equals("pointer")) {
                out.println("@" + (3 + index) + "\nD=M");
            }
            out.println("@SP\nA=M\nM=D\n@SP\nM=M+1");

        } else if (command == Parser.C_POP) {
            if (isBaseSegment(segment)) {
                out.println("@" + getSegmentPointer(segment) + "\nD=M\n@" + index + "\nD=D+A\n@R13\nM=D");
                out.println("@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D");
            } else if (segment.equals("temp") || segment.equals("pointer")) {
                out.println("@SP\nAM=M-1\nD=M");
                if (segment.equals("temp")) out.println("@" + (5 + index));
                if (segment.equals("pointer")) out.println("@" + (3 + index));
                out.println("M=D");
            }
        }
    }

    private boolean isBaseSegment(String seg) {
        return seg.equals("local") || seg.equals("argument") || seg.equals("this") || seg.equals("that");
    }

    private String getSegmentPointer(String seg) {
        if (seg.equals("local")) return "LCL";
        if (seg.equals("argument")) return "ARG";
        if (seg.equals("this")) return "THIS";
        return "THAT";
    }

    public void close() { if (out != null) out.close(); }
}