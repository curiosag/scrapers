package google.maps.responseparser.DIYattempt;

import java.util.Objects;

public class Symbol {
    public static final Symbol EOF = new Symbol(SymType.EOF);
    public static final Symbol NULL = new Symbol(SymType.NULL);
    public static final Symbol LBR = new Symbol(SymType.LBR, "[");
    public static final Symbol RBR = new Symbol(SymType.RBR, "]");
    public static final Symbol COMMA = new Symbol(SymType.COMMA, ",");
    public static final Symbol ERROR = new Symbol(SymType.ERROR, "");

    public final SymType type;
    public final String value;

    private Symbol(SymType type, String value) {
        this.type = type;
        this.value = value;
    }

    private Symbol(SymType type) {
        this.type = type;
        this.value = null;
    }

    public static Symbol stringSym(String value)
    {
        return new Symbol(SymType.STRING, value);
    }

    public static Symbol numSym(String value)
    {
        return new Symbol(SymType.NUMBER, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return type == symbol.type && Objects.equals(value, symbol.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
