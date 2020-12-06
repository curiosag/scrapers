package google.maps.responseparser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static google.maps.responseparser.Symbol.*;

public class ResponseScannerTest {

    @Test
    public void test() {
        assertEquals(EOF, scan(null).get(0));
        assertEquals(EOF, scan("").get(0));
        assertEquals(List.of(NULL, EOF), scan("null"));
        assertEquals(List.of(stringSym("a"), EOF), scan("\"a\""));
        assertEquals(List.of(stringSym("a"), EOF), scan("\"a")); // fuck non terminated strings
        assertEquals(List.of(stringSym("a"), ERROR), scan("\"a\"a"));
        assertEquals(List.of(stringSym("ஸ்ரீமந்தவெளியம்மன"), EOF), scan("\"ஸ்ரீமந்தவெளியம்மன\""));
        assertEquals(List.of(numSym("1.1"), EOF), scan("1.1"));

        assertEquals(List.of(numSym("1"), ERROR), scan("1a"));
        assertEquals(List.of(numSym("1"), COMMA, numSym("1.1"), LBR, stringSym("a"), LBR, COMMA, NULL, RBR, EOF), scan("1,1.1[\"a\"[,null]"));
    }

    private List<Symbol> scan(String value) {
        List<Symbol> result = new ArrayList<>();
        ResponseScanner s = new ResponseScanner(value);
        Symbol i = s.next();
        result.add(i);
        while (i != EOF && i != ERROR) {
            i = s.next();
            result.add(i);
        }
        return result;
    }
}