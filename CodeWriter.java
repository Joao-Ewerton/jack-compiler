import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String fileName;
    private int labelCounter = 0;

    public CodeWriter(File file) {
        try {
            out = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao criar arquivo: " + file.getAbsolutePath());
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public void writeArithmetic(String command) {
        out.println("// " + command);
        if (command.equals("add") || command.equals("sub") || command.equals("and") || command.equals("or")) {
            out.println("@SP");
            out.println("AM=M-1");
            out.println("D=M");
            out.println("A=A-1");
            
            if (command.equals("add")) out.println("M=M+D");
            else if (command.equals("sub")) out.println("M=M-D");
            else if (command.equals("and")) out.println("M=M&D");
            else if (command.equals("or")) out.println("M=M|D");
            
        } else if (command.equals("neg") || command.equals("not")) {
            out.println("@SP");
            out.println("A=M-1");
            
            if (command.equals("neg")) out.println("M=-M");
            else if (command.equals("not")) out.println("M=!M");
            
        } else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
            String labelTrue = "TRUE_" + labelCounter;
            String labelEnd = "END_" + labelCounter;
            labelCounter++;

            out.println("@SP");
            out.println("AM=M-1");
            out.println("D=M");
            out.println("A=A-1");
            out.println("D=M-D");

            out.println("@" + labelTrue);
            if (command.equals("eq")) out.println("D;JEQ");
            else if (command.equals("gt")) out.println("D;JGT");
            else if (command.equals("lt")) out.println("D;JLT");

            out.println("@SP");
            out.println("A=M-1");
            out.println("M=0"); // False
            out.println("@" + labelEnd);
            out.println("0;JMP");

            out.println("(" + labelTrue + ")");
            out.println("@SP");
            out.println("A=M-1");
            out.println("M=-1"); // True

            out.println("(" + labelEnd + ")");
        }
    }

    public void writePushPop(int command, String segment, int index) {
        out.println("// " + (command == Parser.C_PUSH ? "push" : "pop") + " " + segment + " " + index);
        
        if (command == Parser.C_PUSH) {
            if (segment.equals("constant")) {
                out.println("@" + index);
                out.println("D=A");
            } else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
                out.println("@" + getSegmentPointer(segment));
                out.println("D=M");
                out.println("@" + index);
                out.println("A=D+A");
                out.println("D=M");
            } else if (segment.equals("temp")) {
                out.println("@" + (5 + index));
                out.println("D=M");
            } else if (segment.equals("pointer")) {
                out.println("@" + (3 + index));
                out.println("D=M");
            } else if (segment.equals("static")) {
                out.println("@" + fileName + "." + index);
                out.println("D=M");
            }

            out.println("@SP");
            out.println("A=M");
            out.println("M=D");
            out.println("@SP");
            out.println("M=M+1");

        } else if (command == Parser.C_POP) {
            if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
                out.println("@" + getSegmentPointer(segment));
                out.println("D=M");
                out.println("@" + index);
                out.println("D=D+A");
                out.println("@R13");
                out.println("M=D");
                
                out.println("@SP");
                out.println("AM=M-1");
                out.println("D=M");
                
                out.println("@R13");
                out.println("A=M");
                out.println("M=D");
                
            } else if (segment.equals("temp") || segment.equals("pointer") || segment.equals("static")) {
                out.println("@SP");
                out.println("AM=M-1");
                out.println("D=M");
                
                if (segment.equals("temp")) out.println("@" + (5 + index));
                else if (segment.equals("pointer")) out.println("@" + (3 + index));
                else if (segment.equals("static")) out.println("@" + fileName + "." + index);
                
                out.println("M=D");
            }
        }
    }

    private String getSegmentPointer(String segment) {
        if (segment.equals("local")) return "LCL";
        if (segment.equals("argument")) return "ARG";
        if (segment.equals("this")) return "THIS";
        if (segment.equals("that")) return "THAT";
        return "";
    }

    public void close() {
        if (out != null) out.close();
    }
}