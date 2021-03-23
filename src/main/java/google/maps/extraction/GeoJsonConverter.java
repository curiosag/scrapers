package google.maps.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

record GeoRecord(String name, String supName, String geo) {
}

public class GeoJsonConverter {

    public static void main(String[] args) throws IOException {
        List<String> sql = toSql("Region", null, "/home/ssmertnig/dev/data/temples/qgisstuff/nepal.json");
        write(sql, "/home/ssmertnig/dev/data/temples/qgisstuff/nepal.sql");
    }


    private static void write(List<String> values, String targetPath) {
        try (FileWriter writer = new FileWriter(targetPath)) {
            for (String v : values) {
                writer.write(v + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> toSql(String nameProperty, String supNameProperty, String fileIn) {
        Path pathIn = Path.of(fileIn);

        try {
            List<GeoRecord> entries = extractJson(nameProperty, supNameProperty, Files.readString(pathIn));

            List<String> statements = new ArrayList<>();
            for (GeoRecord r : entries) {
                statements.add(toSqlInsertStmt(r));
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toSqlInsertStmt(GeoRecord r) {
        String query = """              
                INSERT INTO lola_region_staging (name, name_sup, geom) values('%s', '%s', ST_GeomFromGeoJSON('{"type":"Multipolygon","coordinates":[%s]}'));               
                """;
        return String.format(query, r.name(), r.supName(), r.geo());
    }

    private static String toMultiPolygon(String type, JsonNode coords) {
        String coo = coords.toString().replace(",0.0]", "]");

        return switch (type) {
            case "Polygon" -> coo;
            case "MultiPolygon" -> clipBraces(coo);
            default -> throw new IllegalStateException("unknow geometry " + type);
        };
    }

    private static String clipBraces(String coords) {
        return coords.substring(1, coords.length() - 1);
    }

    private static List<GeoRecord> extractJson(String nameProperty, String supNameProperty, String json) {
        List<GeoRecord> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodes = mapper.readTree(json).get("features");
            if (nodes == null) {
                return Collections.emptyList();
            } else {
                nodes.forEach(n -> {
                    String name = n.get("properties").get(nameProperty).textValue();
                    String supName = supNameProperty != null ? n.get("properties").get(supNameProperty).textValue() : null;
                    JsonNode geo = n.get("geometry").get("coordinates");
                    String type = n.get("geometry").get("type").textValue();
                    result.add(new GeoRecord(name, supName, toMultiPolygon(type, geo)));
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
