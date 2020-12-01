package google.maps;

import util.HttpUtil;

import java.util.Optional;

public class Nearbysearch {

    float lat = 25.2978704f;
    float lon = 83.0065732f;
    int radius = 50;

    //URIPatter with 2 placeholders for latitude/longitude and one for radius
    private final static String searchPattern = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%.5f,%.5f&radius=%d&type=hindu_temple&key=AIzaSyBV78ab8nLSZJgQ003HmjWIN84sMz3gwqo";

    // nextpage if page toke is given
    final static String nextPage = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=%s&key=AIzaSyBV78ab8nLSZJgQ003HmjWIN84sMz3gwqo";

    public static void main(String[] args) {

      //  FileUtil.write(result, "/home/ssmertnig/dev/data/temples/b.txt");
    }

    static Optional<String> getPageToken(String json) {
        String tokenmarker = "\"next_page_token\" : \"";
        int markerIndex = json.indexOf(tokenmarker);
        if (markerIndex < 0) {
            return Optional.empty();
        }

        StringBuilder sb = new StringBuilder();
        int i = markerIndex + tokenmarker.length();
        char current = ' ';
        while (i < json.length()) {
            current = json.charAt(i);
            if (current == '"')
                break;
            else
                sb.append(current);
            i++;
        }
        return Optional.of(sb.toString());
    }

    String getNearby(float latitude, float longitude, int radius) {
        return HttpUtil.getByUrlConnection(String.format(searchPattern, latitude, longitude, radius));
    }

    String getNext(String pageToken) {
        return HttpUtil.getByUrlConnection(String.format(nextPage, pageToken));
    }

    //https://stackoverflow.com/questions/7477003/calculating-new-longitude-latitude-from-old-n-meters
    private static final double radius_earth = 6371000.0;

    static double mvLat(double from_latitude, double m){
        return from_latitude  + (m / radius_earth) * (180d / Math.PI);
    }

    static double mvLon(double from_longitude, double from_latitude, double m){
        return from_longitude + (m / radius_earth) * (180d / Math.PI) / Math.cos(from_latitude * Math.PI/180d);
    }

}
