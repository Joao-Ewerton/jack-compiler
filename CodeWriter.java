import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String fileName;
    private int labelCounter = 0;
    private int returnCounter = 0;

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

            out.println("@SP\nA=M-1\nM=0\n@" + labelEnd + "\n0;JMP");
            out.println("(" + labelTrue + ")\n@SP\nA=M-1\nM=-1");
            out.println("(" + labelEnd + ")");
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
            } else if (segment.equals("static")) {
                out.println("@" + fileName + "." + index + "\nD=M"); // Lógica exclusiva do StaticTest!
            }
            
            out.println("@SP\nA=M\nM=D\n@SP\nM=M+1");

        } else if (command == Parser.C_POP) {
            if (isBaseSegment(segment)) {
                out.println("@" + getSegmentPointer(segment) + "\nD=M\n@" + index + "\nD=D+A\n@R13\nM=D");
                out.println("@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D");
            } else if (segment.equals("temp") || segment.equals("pointer") || segment.equals("static")) {
                out.println("@SP\nAM=M-1\nD=M");
                
                if (segment.equals("temp")) out.println("@" + (5 + index));
                else if (segment.equals("pointer")) out.println("@" + (3 + index));
                else if (segment.equals("static")) out.println("@" + fileName + "." + index); // Gravação do StaticTest!
                
                out.println("M=D");
            }
        }
    }

    //NOVOS MÉTODOS PARA CONTROLE DE FLUXO
    public void writeLabel(String label) {
        out.println("// label " + label);
        // Formato oficial do Hack Assembly para declaração de label
        out.println("(" + label + ")");
    }

    public void writeGoto(String label) {
        out.println("// goto " + label);
        // Salto incondicional
        out.println("@" + label);
        out.println("0;JMP");
    }

    public void writeIf(String label) {
        out.println("// if-goto " + label);
        // Retira o valor do topo da pilha (SP--)
        out.println("@SP");
        out.println("AM=M-1");
        out.println("D=M");
        // Se D não for falso (D != 0), salta para o label
        out.println("@" + label);
        out.println("D;JNE"); 
    }

    //NOVOS MÉTODOS: SUB-ROTINAS
    public void writeFunction(String functionName, int numLocals) {
        out.println("// function " + functionName + " " + numLocals);
        out.println("(" + functionName + ")");
        // Inicializa as variáveis locais (LCL) com 0
        for (int i = 0; i < numLocals; i++) {
            out.println("@SP\nA=M\nM=0\n@SP\nM=M+1");
        }
    }

    public void writeCall(String functionName, int numArgs) {
        String returnLabel = functionName + "$ret." + returnCounter;
        returnCounter++;
        out.println("// call " + functionName + " " + numArgs);

        out.println("@" + returnLabel + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1");
        out.println("@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");
        out.println("@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");
        out.println("@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");
        out.println("@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");

        out.println("@SP\nD=M\n@5\nD=D-A\n@" + numArgs + "\nD=D-A\n@ARG\nM=D");

        out.println("@SP\nD=M\n@LCL\nM=D");

        out.println("@" + functionName + "\n0;JMP");

        out.println("(" + returnLabel + ")");
    }

    public void writeReturn() {
        out.println("// return");
        out.println("@LCL\nD=M\n@R13\nM=D");

        out.println("@5\nA=D-A\nD=M\n@R14\nM=D");

        out.println("@SP\nAM=M-1\nD=M\n@ARG\nA=M\nM=D");

        out.println("@ARG\nD=M+1\n@SP\nM=D");

        out.println("@R13\nAM=M-1\nD=M\n@THAT\nM=D");
        out.println("@R13\nAM=M-1\nD=M\n@THIS\nM=D");
        out.println("@R13\nAM=M-1\nD=M\n@ARG\nM=D");
        out.println("@R13\nAM=M-1\nD=M\n@LCL\nM=D");

        out.println("@R14\nA=M\n0;JMP");
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