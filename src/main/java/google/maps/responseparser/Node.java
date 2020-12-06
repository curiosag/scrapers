package google.maps.responseparser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Node {
    public static final Node NULL = new Node(Symbol.NULL, null);
    public final Symbol symbol;
    public final List<Node> elements;

    public Node(Symbol symbol, List<Node> elements) {
        this.symbol = symbol;
        this.elements = elements;
    }

    public static Node terminal(Symbol symbol) {
        return new Node(symbol, null);
    }

    public static Node nonTerminal(List<Node> elements) {
        return new Node(null, elements);
    }

    public String value() {
        return symbol != null ? symbol.value : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(symbol, node.symbol) && Objects.equals(elements, node.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, elements);
    }

    private String prettyPrintNode(Stack<Integer> nodeNumbers) {
        String indentation = "  ".repeat(nodeNumbers.size());
        String nodeEnum = getEnumeration(nodeNumbers);
        String intro = indentation + nodeEnum + ":{symbol=" + (symbol == null ? "null" : symbol.value) + ", elements=";

        if (elements == null) return intro + "null}";
        if (elements.size() == 0) return intro + "[]}";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            sb.append(indentation);
            nodeNumbers.push(i);
            sb.append(elements.get(i).prettyPrintNode(nodeNumbers));
            nodeNumbers.pop();
            sb.append("\n");
        }
        return intro + " ".repeat(nodeEnum.length()  + 1) + "\n" + sb.toString() + indentation + "}";
    }

    private String getEnumeration(Stack<Integer> nums) {
        return nums.stream().map(Object::toString).collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
        return prettyPrintNode(new Stack<>());
    }

    public Optional<String> getValue(Integer ... path) {
        return getValue(Stream.of(path).collect(Collectors.toList()));
    }

    public Optional<String> getValue(List<Integer> path) {
        if (path.size() == 0)
        {
            return symbol == null ? Optional.empty() : Optional.ofNullable(symbol.value);
        } else {
            int current = path.get(0);
            if(elements == null || current < 0 || current > elements.size() - 1)
            {
                return Optional.empty();
            }
            return elements.get(current).getValue(path.subList(1, path.size()));
        }
    }

}
