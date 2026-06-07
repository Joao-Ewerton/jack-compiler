import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String fileName;
    private int labelCounter = 0;

    public CodeWriter(File file) {
        try { out = new PrintWriter(file); } 
        catch (FileNotFoundException e) {}
    }

    public void setFileName(String fileName) {
        this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public void writeArithmetic(String command) {
        out.println("// " + command);
        if (command.equals("add") || command.equals("sub") || command.equals("and") || command.equals("or")) {
            out.println("@SP\nAM=M-1\nD=M\nA=A-1");
            if (command.equals("add")) out.println("M=M+D");
            else if (command.equals("sub")) out.println("M=M-D");
            else if (command.equals("and")) out.println("M=M&D");
            else if (command.equals("or")) out.println("M=M|D");
            
        } else if (command.equals("neg") || command.equals("not")) {
            out.println("@SP\nA=M-1");
            if (command.equals("neg")) out.println("M=-M");
            else if (command.equals("not")) out.println("M=!M");
            
        } else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
            String labelTrue = "TRUE_" + labelCounter;
            String labelEnd = "END_" + labelCounter;
            labelCounter++;

            out.println("@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D");
            out.println("@" + labelTrue);
            if (command.equals("eq")) out.println("D;JEQ");
            else if (command.equals("gt")) out.println("D;JGT");
            else if (command.equals("lt")) out.println("D;JLT");

            out.println("@SP\nA=M-1\nM=0\n@" + labelEnd + "\n0;JMP"); // False
            out.println("(" + labelTrue + ")\n@SP\nA=M-1\nM=-1"); // True
            out.println("(" + labelEnd + ")");
        }
    }

    public void writePushPop(int command, String segment, int index) {
        out.println("// " + (command == Parser.C_PUSH ? "push" : "pop") + " " + segment + " " + index);
        if (command == Parser.C_PUSH) {
            if (segment.equals("constant")) out.println("@" + index + "\nD=A");
            else if (isBaseSegment(segment)) out.println("@" + getSegmentPointer(segment) + "\nD=M\n@" + index + "\nA=D+A\nD=M");
            else if (segment.equals("temp")) out.println("@" + (5 + index) + "\nD=M");
            else if (segment.equals("pointer")) out.println("@" + (3 + index) + "\nD=M");
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

    private boolean isBaseSegment(String seg) { return seg.equals("local") || seg.equals("argument") || seg.equals("this") || seg.equals("that"); }
    private String getSegmentPointer(String seg) {
        if (seg.equals("local")) return "LCL";
        if (seg.equals("argument")) return "ARG";
        if (seg.equals("this")) return "THIS";
        return "THAT";
    }
    public void close() { if (out != null) out.close(); }
}