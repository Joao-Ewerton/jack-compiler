import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String fileName;

    public CodeWriter(File file) {
        try {
            out = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao criar arquivo de saída.");
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public void writeArithmetic(String command) {
    }

    public void writePushPop(int command, String segment, int index) {
    }

    public void close() {
        if (out != null) out.close();
    }
}