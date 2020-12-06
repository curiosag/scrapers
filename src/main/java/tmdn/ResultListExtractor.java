package tmdn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultListExtractor {

    public static List<TmdRow> getFrom(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodes = mapper.readTree(json).get("rows");

            List<TmdRow> result = new ArrayList<>();

            nodes.forEach(n -> {
                TmdRow row = new TmdRow();
                result.add(row);
                row.setIndicationOfProduct(getFirst(n, "ipvalue"));
                row.setSt13(get(n, "ST13"));
                row.setDesignNumber(get(n, "did"));
                row.setOwnerName(getFirst(n, "ApplicantName"));
                row.setRepresentativeName(getFirst(n, "RepresentativeName"));

                row.setDesignOffice(get(n, "oc"));

                String tp = "";
                JsonNode tpitems = n.get("tp");
                for (int i = 0; i < tpitems.size(); i++) {
                    tp = tp + tpitems.get(i).asText() + ' ';
                }
                row.setDesignatedTerritory(tp);

                row.setLocarnoClassification(getFirst(n, "lc"));
                row.setStatus(get(n, "sc"));

                row.setApplicationDate(fmtDate(get(n, "ad")));
                row.setRegistrationDate(fmtDate(get(n, "RegistrationDate")));
                row.setPublicationDate(fmtDate(get(n, "pubd")));
                row.setExpiryDate(fmtDate(get(n, "expd")));

                row.setUrlOwnerDetails(getFirst(n, "auri"));
                row.setUrlRepresentativeDetails(getFirst(n, "ruri"));
            });

            return result;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String get(JsonNode n, String key) {
        return n.get(key).asText();
    }

    private static String getFirst(JsonNode n, String key) {
        return n.get(key).get(0).asText();
    }

    private final static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    private static String fmtDate(String millis) {
        Long m = Long.parseLong(millis);
        Date date = new Date(m);
        return fmt.format(date);
    }
}
