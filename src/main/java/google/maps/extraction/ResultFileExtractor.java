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
        extractFromCsv( "/home/ssmertnig/dev/data/temples/todo/baliU.csv", "/home/ssmertnig/dev/data/temples/todo/bali.sql", ResultFileExtractor::getSql);
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

    private static final String insertPlace = "insert into places_scraped (id, place_id, name, plus_compound_code, global_code, vicinity, geom) values ('%s', '%s','%s','%s','%s','%s', ST_GeomFromText('POINT(%.14f %.14f)')) ON CONFLICT (place_id) DO NOTHING;\n";

    static int maxId = 13255;
    public static List<String> getSql(PlaceSearchResultItem i) {
        List<String> result = new ArrayList<>();
        result.add(String.format(insertPlace, ++maxId, i.placeId, quote(i.name),
                i.plus_compound_code, quote(i.adress), quote(i.vicinity), i.lat, i.lon));
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

    static List<PlaceSearchResultItem> fromCsv(String inputPath) {
        List<PlaceSearchResultItem> result = new ArrayList<>();
        try (FileReader filereader = new FileReader(inputPath)) {
            CSVReader csvReader = new CSVReader(filereader, ',', '"');

            String[] parts;
            while ((parts = csvReader.readNext()) != null) {
                if(parts.length == 0)
                    continue;
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
