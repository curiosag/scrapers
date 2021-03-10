package google.maps.webview;

import boofcv.struct.feature.Match;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class ImageTemplateMatchingTest {

    @Test
    public void getTemplateLocations() {
        String markedImagePath = "/home/ssmertnig/temp/";
        File template = new File("/home/ssmertnig/dev/repo/scrapers/src/test/resources/om.png");
        File image = new File("/home/ssmertnig/dev/repo/scrapers/src/test/resources/testimage.png");

        List<Match> locations = new ImageTemplateMatching(markedImagePath)
                .getTemplateLocations(template, image);
        assertEquals(2, locations.size());
    }

    private File getFileFromResource(String fileName){
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

}