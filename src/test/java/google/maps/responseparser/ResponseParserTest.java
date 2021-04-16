package google.maps.responseparser;

import google.maps.responseparser.DIYattempt.Node;
import google.maps.responseparser.DIYattempt.ResponseParser;
import google.maps.responseparser.DIYattempt.Symbol;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static google.maps.responseparser.DIYattempt.Node.nonTerminal;
import static google.maps.responseparser.DIYattempt.Node.terminal;

public class ResponseParserTest {

    private final static List<Node> empty = Collections.emptyList();

    @Test
    public void nowReally() throws IOException {
        String path = "/home/ssmertnig/dev/data/temples/scraped/responses/0.txt";
        String value = Files.readString(Paths.get(path));
        String v = new ResponseParser(value).parse().toString();
        v = null;
    }

    @Test
    public void testParse() {
        assertTrue(parse(null).isEmpty());
        assertTrue(parse("").isEmpty());
        assertTrue(parse("null").isEmpty());
        assertEquals(empty, parse("[]"));
        assertEquals(Collections.singletonList(nonTerminal(empty)), parse("[[]]"));
        assertEquals(List.of(nonTerminal(empty), nonTerminal(empty)), parse("[[],[]]"));

        List<Node> expected = new ArrayList<>();
        expected.add(terminal(Symbol.stringSym("a")));
        expected.add(nonTerminal(Collections.singletonList(terminal(Symbol.numSym("1.1")))));
        expected.add(terminal(Symbol.NULL));
        expected.add(terminal(Symbol.stringSym("b")));
        expected.add(nonTerminal(Collections.singletonList(Node.NULL)));
        expected.add(nonTerminal(List.of(nonTerminal(empty), nonTerminal(empty))));
        assertEquals(expected, parse("[\"a\",[1.1],null,\"b\",[null],[[],[]]]"));
    }

    private List<Node> parse(String value) {
        return new ResponseParser(value).parse().elements;
    }
}

