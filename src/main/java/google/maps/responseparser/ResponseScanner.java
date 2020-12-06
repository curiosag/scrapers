package google.maps.responseparser;

import java.util.HashMap;
import java.util.Map;

import static google.maps.responseparser.Symbol.*;

public class ResponseScanner {

    private static final String skipSequence = ")]}'\n";

    private static final char lBr = '[';
    private static final char rBr = ']';
    private static final char comma = ',';
    private static final char nl = '\n';

    private final String content;
    private final int maxIdx;
    int idx;

    boolean eof;
    boolean error;

    public ResponseScanner(String content) {
        this.content = content;
        maxIdx = content == null ? -1 : content.length() - 1;
        eof = content == null || content.length() == 0;
        if (content != null && content.startsWith(skipSequence)) {
            idx = idx + skipSequence.length();
        }
    }

    private boolean isWhitespace(int idx) {
        return content.charAt(idx) == nl;
    }

    private static final Map<Character, Symbol> structMarkers = new HashMap<>();

    static {
        structMarkers.put(lBr, Symbol.LBR);
        structMarkers.put(rBr, Symbol.RBR);
        structMarkers.put(comma, Symbol.COMMA);
    }

    public Symbol next() {
        if (error) {
            return Symbol.ERROR;
        }
        if (eof()) {
            return Symbol.EOF;
        }

        while (!eof() && isWhitespace(idx)) {
            idx++;
        }
        if (eof()) {
            return Symbol.EOF;
        }

        char c = content.charAt(idx);
        if (c == lBr || c == rBr || c == comma) {
            idx++;
            return structMarkers.get(c);
        }
        if (c == '"') {
            return readString();
        }
        if (isNumber(idx)) {
            return readNumber();
        }
        if (readNull()) {
            return Symbol.NULL;
        }
        {
            error = true;
            return Symbol.ERROR;
        }
    }

    private Symbol readString() {
        idx++;
        char c = content.charAt(idx);
        int start = idx;
        while (c != '"') {
            idx++;
            if (idx > maxIdx) {
                eof = true;
                break;
            }
            c = content.charAt(idx);
        }
        return stringSym(content.substring(start, idx++));
    }

    private Symbol readNumber() {
        int start = idx;
        char c = content.charAt(idx);
        while ((isNumber(c) || c == '.') && !eof) {
            idx++;
            if (idx > maxIdx) {
                eof = true;
                break;
            }
            c = content.charAt(idx);
        }
        return numSym(content.substring(start, idx));
    }

    private boolean readNull() {
        if (content.startsWith("null", idx)) {
            idx = idx + 4;
            return true;
        } else {
            return false;
        }
    }

    private boolean eof() {
        return eof ? eof : idx > maxIdx;
    }

    private boolean isNumber(int idx) {
        return isNumber(content.charAt(idx));
    }

    private boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

}
