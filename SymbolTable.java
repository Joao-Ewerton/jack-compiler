import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    public static class Symbol {
        private String type;
        private Kind kind;
        private int index;

        public Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String type() { return type; }
        public Kind kind() { return kind; }
        public int index() { return index; }
    }

    private Map<String, Symbol> classScope;
    private Map<String, Symbol> subroutineScope;
    private Map<Kind, Integer> indices;

    public SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        indices = new HashMap<>();
        
        indices.put(Kind.STATIC, 0);
        indices.put(Kind.FIELD, 0);
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    public void startSubroutine() {
        subroutineScope.clear();
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    public void define(String name, String type, Kind kind) {
        int index = indices.get(kind);
        Symbol symbol = new Symbol(type, kind, index);

        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            classScope.put(name, symbol);
        } else if (kind == Kind.ARG || kind == Kind.VAR) {
            subroutineScope.put(name, symbol);
        }
        
        indices.put(kind, index + 1);
    }

    public int varCount(Kind kind) {
        return indices.getOrDefault(kind, 0);
    }

    public Symbol resolve(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name);
        } else if (classScope.containsKey(name)) {
            return classScope.get(name);
        }
        return null;
    }
}