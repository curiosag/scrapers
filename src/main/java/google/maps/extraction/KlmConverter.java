package google.maps.extraction;

import java.io.*;
import java.util.Optional;

public class KlmConverter {

    private final static String fileIn = "/home/ssm/dev/data/temples/Longitude_Graticules_and_World_Countries_Boundaries.kml";
    private final static String fileOut = "/home/ssm/dev/data/temples/Longitude_Graticules_and_World_Countries_Boundaries.sql";

    private final static String tagName0 = "<SimpleData name=\"CNTRY_NAME\">";
    private final static String tagName1 = "<SimpleData name=\"NAME_1\">";
    private final static String tagName2 = "<SimpleData name=\"NAME_2\">";
    private final static String tagType = "<SimpleData name=\"ENGTYPE_2\">";
    private final static String tagPolygon = "<Polygon><outerBoundaryIs><LinearRing><coordinates>";
    private final static int typeState = 1;
    private final static int typeDistrict = 2;

    public static void main(String[] args) throws IOException {
        final String[] name0 = new String[1];
        final String[] name1 = new String[1];
        final String[] name2 = new String[1];
        final String[] polygon = new String[1];
        final int[] type = new int[1];

        try (BufferedReader r = new BufferedReader(new FileReader(fileIn)); FileWriter w = new FileWriter(fileOut)) {
            String line = r.readLine();
            while (line != null) {
                scanFor(tagName0, line).ifPresent(t -> name0[0] = t);
                scanFor(tagName1, line).ifPresent(t -> name1[0] = t);
                scanFor(tagName2, line).ifPresent(t -> name2[0] = t);
                scanFor(tagType, line).ifPresent(t -> {
                    if (!"District".equals(t)) throw new IllegalStateException();
                    type[0] = 2;
                });
                scanFor(tagPolygon, line).ifPresent(t -> polygon[0] = flipLonLat(t));

                if (polygon[0] != null) {
                    int typecode = type[0] == typeDistrict ? typeDistrict : typeState; //state or district
                    typecode = name0[0] == null? typecode: 0; //country
                    String out = String.format("insert into regions (name0, name1, name2, type, geom) values ('%s', '%s','%s',%d,ST_GeomFromText('POLYGON((%s))'));\n", name0[0], name1[0], name2[0], typecode, polygon[0]);
                    System.out.println(".");
                    w.write(out);
                    name0[0] = null;
                    name1[0] = null;
                    name2[0] = null;
                    type[0] = 0;
                    polygon[0] = null;
                }

                line = r.readLine();
            }
        }
    }

    private static String flipLonLat(String t) {
        StringBuilder b = new StringBuilder();
        String[] lonlat = t.split(" ");
        for (int i = 0; i < lonlat.length; i++) {
            String[] parts = lonlat[i].split(",");
            b.append(parts[1]);
            b.append(' ');
            b.append(parts[0]);
            if (i < lonlat.length - 1) {
                b.append(',');
            }
        }
        return b.toString();
    }

    static Optional<String> scanFor(String tag, String line) {
        if (line != null) {
            int i = line.indexOf(tag);
            if (i > 0) {
                int j = line.indexOf("<", i + tag.length());
                if (j < 0) {
                    throw new IllegalStateException();
                }
                return Optional.of(line.substring(i + tag.length(), j));
            }
        }
        return Optional.empty();
    }

}