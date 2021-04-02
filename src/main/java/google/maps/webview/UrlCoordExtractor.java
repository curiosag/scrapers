package google.maps.webview;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import google.maps.Point;

public class UrlCoordExtractor {

    private final static String regex = ".*?!3d(-*[0-9.]*)!4d(-*[0-9.]*)";

    /* for longitude/latitude in the form of ...!2d76.9987!3d23.344455!...
     .*?        ... anything, non greedy
     !3d        ... "!3d"
     (-*[0-9.]*)  ... a float: maybe negative, digit or dot, multiple times
     !4d        ... "!4d"
     (-*[0-9.]*)  ... another float
    * */
    private final static Pattern pattern = Pattern.compile(regex);

    public static Optional<Point> extract(String url) {
        if(url == null)
        {
            return Optional.empty();
        }
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            MatchResult r = m.toMatchResult();
            if (r.groupCount() == 2) {
                try {
                    double lon = Double.parseDouble(r.group(2));
                    double lat = Double.parseDouble(r.group(1));
                    return Optional.of(new Point(lat, lon));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

}
