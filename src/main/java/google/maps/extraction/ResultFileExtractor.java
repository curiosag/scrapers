package google.maps.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import google.maps.responseparser.ResponseExtractor;
import google.maps.searchapi.PlaceSearchResultItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ResultFileExtractor {

    public final static String resultFilePath = "./scraped/responses";

    public static void main(String[] args) throws SQLException {
        extractFromCsv( "/home/ssm/dev/data/temples/SriLanka2_2.csv", "/home/ssm/dev/data/temples/SriLanka2_2.sql", ResultFileExtractor::getSql);
    }

    private static void extractFromCsv(String csvInputPath, String outputFileName, Function<PlaceSearchResultItem, List<String>> outputGenerator) {
        final int[] count = new int[1];
        char del = 8;
        try (FileWriter w = new FileWriter(outputFileName)) {
            fromCsv(csvInputPath).forEach( i -> {
                outputGenerator.apply(i).forEach(line -> {
                    try {
                        w.write(line);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                for (int j = 0; j < String.valueOf(count[0]).length(); j++) {
                    System.out.print(del);
                }
                count[0]++;
                System.out.print(count[0]);

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void extractToFile(String inputPath, String outputFileName, Function<PlaceSearchResultItem, List<String>> outputGenerator) {
        final int[] count = new int[1];
        char del = 8;
        try (FileWriter w = new FileWriter(outputFileName)) {
            extractFromFiles(inputPath, i -> {
                outputGenerator.apply(i).forEach(line -> {
                    try {
                        w.write(line);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                for (int j = 0; j < String.valueOf(count[0]).length(); j++) {
                    System.out.print(del);
                }
                count[0]++;
                System.out.print(count[0]);

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String csvRowTemplate = "%s,\"%s\",\"%s\",\"%s\",\"%s\",%.14f,%.14f\n";

    public static String getCsv(PlaceSearchResultItem i) {
        return String.format(csvRowTemplate, i.placeId, i.name, i.plus_compound_code, i.adress,
                i.vicinity, i.lat, i.lon);
    }

    private static final String insertPlaceGeom = "insert into places_geo (place_id, geom) values ('%s',ST_GeomFromText('POINT(%.14f %.14f)')) ON CONFLICT (place_id) DO NOTHING;\n";
    private static final String insertPlace = "insert into places (place_id, name, plus_compound_code, global_code, vicinity) values ('%s','%s','%s','%s','%s') ON CONFLICT (place_id) DO NOTHING;\n";

    public static List<String> getSql(PlaceSearchResultItem i) {
        List<String> result = new ArrayList<>();
        result.add(String.format(insertPlaceGeom, i.placeId, i.lat, i.lon));
        result.add(String.format(insertPlace, i.placeId, quote(i.name),
                i.plus_compound_code, quote(i.adress), quote(i.vicinity)));
        return result;
    }

    private static String quote(String s) {
        return s.replaceAll("'", "''").replaceAll("\"", "''");
    }



    /* preview number of distinct places:
       grep -nri "place_id" |  awk'{ print $4}' | sort | uniq | wc -l
    * */

    private static void extractFromFiles(String path, Consumer<PlaceSearchResultItem> sink) throws IOException {
        Set<String> locationIds = new HashSet<>();

        String[] files = (new File(path)).list((f, name) -> name.contains(".txt"));
        if (files == null) {
            throw new IllegalStateException();
        }

        for (String file : files) {
            Optional<PlaceSearchResultItem> item = ResponseExtractor.extract(Files.readString(Path.of(path + file)));
            item.ifPresent(i -> {
                if (!locationIds.contains(i.placeId)) {
                    sink.accept(i);
                    locationIds.add(i.placeId);
                }
            });
        }
    }

    private static List<PlaceSearchResultItem> extractFromAPIJson(String json) {
        List<PlaceSearchResultItem> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodes = mapper.readTree(json).get("results");
            if (nodes == null) {
                return Collections.emptyList();
            } else {
                nodes.forEach(n -> result.add(extractNode(n)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static PlaceSearchResultItem extractNode(JsonNode n) {
        String placeId = n.get("place_id").textValue();
        String name = n.get("name").textValue();
        JsonNode geo = n.get("geometry").get("location");
        double lat = geo.get("lat").doubleValue();
        double lng = geo.get("lng").doubleValue();
        JsonNode plus_code = n.get("plus_code");
        String plus_compound_code = plus_code == null ? "" : getText(plus_code, "compound_code");
        String global_code = plus_code == null ? "" : getText(plus_code, "global_code");
        String vicinity = getText(n, "vicinity");
        return new PlaceSearchResultItem(placeId, lat, lng, name, plus_compound_code, global_code, vicinity);
    }

    private static String getText(JsonNode n, String key) {
        JsonNode la = n.get(key);
        return la == null ? "" : la.asText();
    }

    static List<PlaceSearchResultItem> fromCsv(String inputPath) {
        List<PlaceSearchResultItem> result = new ArrayList<>();
        try (FileReader filereader = new FileReader(inputPath)) {
            CSVReader csvReader = new CSVReader(filereader, ',', '"');

            String[] parts;
            while ((parts = csvReader.readNext()) != null) {
                String placeid = parts[0];
                String name = parts[1];
                String plusCode = parts[2];
                String address = parts[3];
                String vicinity = parts[4];
                double lat = Double.parseDouble(parts[5]);
                double lon = Double.parseDouble(parts[6]);
                result.add(new PlaceSearchResultItem(placeid, lat, lon, name, plusCode, address, vicinity));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
