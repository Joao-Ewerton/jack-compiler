public class VMWriter {
    public enum Segment {
        CONST("constant"), ARG("argument"), LOCAL("local"),
        STATIC("static"), THIS("this"), THAT("that"),
        POINTER("pointer"), TEMP("temp");

        private final String name;
        Segment(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public enum Command {
        ADD("add"), SUB("sub"), NEG("neg"),
        EQ("eq"), GT("gt"), LT("lt"),
        AND("and"), OR("or"), NOT("not");

        private final String name;
        Command(String name) { this.name = name; }
        public String getName() { return name; }
    }

    private StringBuilder out;

    public VMWriter() {
        this.out = new StringBuilder();
    }

    public void writePush(Segment segment, int index) {
        out.append("push ").append(segment.getName()).append(" ").append(index).append("\n");
    }

    public void writePop(Segment segment, int index) {
        out.append("pop ").append(segment.getName()).append(" ").append(index).append("\n");
    }

    public void writeArithmetic(Command command) {
        out.append(command.getName()).append("\n");
    }

    public void writeLabel(String label) {
        out.append("label ").append(label).append("\n");
    }

    public void writeGoto(String label) {
        out.append("goto ").append(label).append("\n");
    }

    public void writeIf(String label) {
        out.append("if-goto ").append(label).append("\n");
    }

    public void writeCall(String name, int nArgs) {
        out.append("call ").append(name).append(" ").append(nArgs).append("\n");
    }

    public void writeFunction(String name, int nLocals) {
        out.append("function ").append(name).append(" ").append(nLocals).append("\n");
    }

    public void writeReturn() {
        out.append("return\n");
    }

    public String vmOutput() {
        return out.toString();
    }
}