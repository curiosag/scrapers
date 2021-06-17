package google.maps.responseparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonResponse {

    private final JsonNode node;

    public JsonResponse(String json) {
        node = readTree(json);
    }

    public Optional<String> getValue(Integer... path) {
        return getValue(node, path);
    }

    public Optional<JsonNode> getNode(Integer... path) {
        return getNode(node, path);
    }

    public static Optional<String> getValue(JsonNode node, Integer... path) {
        List<Integer> pathItems = asList(path);
        return getNode(pathItems, node).map(JsonResponse::getTextValue);
    }

    private static List<Integer> asList(Integer[] path) {
        return Stream.of(path).collect(Collectors.toList());
    }

    public static Optional<JsonNode> getNode(JsonNode node, Integer...path) {
        return getNode(asList(path), node);
    }

    public static Optional<JsonNode> getNode(List<Integer> path, JsonNode node) {
        if (path.size() == 0)
            return Optional.ofNullable(node);

        int idx = path.get(0);
        if (node == null || node.size() == 0 || idx < 0 || idx > node.size() - 1) {
            return Optional.empty();
        }

        List<JsonNode> items = getChildren(node);
        return getNode(path.subList(1, path.size()), items.get(idx));
    }

    public static List<JsonNode> getChildren(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false).collect(Collectors.toList());
    }

    public static String prettyPrint(String json) {
        return getPrettyPrintLines(json).stream().reduce("", (a, b) -> a + b + "\n");
    }

    static List<String> getPrettyPrintLines(String json) {
        List<String> result = new ArrayList<>();
        JsonNode node = readTree(json);
        collect(0, 0, result, node);
        return result;
    }

    private static JsonNode readTree(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (IOException e) {
            System.out.println(json);
            throw new RuntimeException(e);
        }
    }

    private static void collect(int level, int seqNumber, List<String> lines, JsonNode node) {
        lines.add(pad(level) + seqNumber + " " + getTextValue(node));
        for (int i = 0; i < node.size(); i++) {
            collect(level + 1, i, lines, node.get(i));
        }
    }

    public static String getTextValue(JsonNode node) {
        if (node instanceof NullNode)
            return "null";
        if (node instanceof ContainerNode)
            return "container";
        if (node instanceof ValueNode)
            return node.asText();
        return node == null ? "null" : "??? " + node.getClass().getSimpleName() + " ???";
    }

    private static String pad(int num) {
        return "|  ".repeat(num);
    }
}
