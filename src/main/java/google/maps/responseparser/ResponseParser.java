package google.maps.responseparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static google.maps.responseparser.Node.nonTerminal;
import static google.maps.responseparser.SymType.*;

/**
 * useless redundant response parser. the data is json, one big plain nested array
 * Node.getValue is useful, though, to navigage the nested arrays
 *
 * */
public class ResponseParser {

    private final String content;

    public ResponseParser(String content) {
        this.content = content;
    }

    public Node parse() {
        if (content == null || content.length() == 0) {
            return Node.nonTerminal(Collections.emptyList());
        }
        return Node.nonTerminal(parse(new ResponseScanner(content)));
    }

    private List<Node> parse(ResponseScanner scanner) {
        Symbol s = scanner.next();
        if (s.type == LBR) {
            return parseList(scanner);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Node> parseList(ResponseScanner scanner) {
        List<Node> result = new ArrayList<>();

        Symbol s = scanner.next();
        while (s.type != RBR && s.type != EOF && s.type != ERROR) {
            switch (s.type) {
                case COMMA -> {
                }
                case NUMBER, STRING -> result.add(Node.terminal(s));
                case NULL -> result.add(Node.NULL);
                case LBR -> result.add(nonTerminal(parseList(scanner)));
                default -> throw new IllegalStateException();
            }
            s = scanner.next();
        }
        return result;
    }
}