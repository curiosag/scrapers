package google.maps.webview;

import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static boofcv.struct.image.ImageType.SB_F32;

public class ImageTemplateMatching {

    private static final double matchThreshold = -6;
    private final String pathMarkedResultImages;
    int marked = 0;

    /*
     * @param pathMarkedResultImages set to nun-null value to store images with detected matches highlighted
     * */
    public ImageTemplateMatching(String pathMarkedResultImages) {
        this.pathMarkedResultImages = pathMarkedResultImages;
        if (pathMarkedResultImages != null) {
            String[] dir = new File(pathMarkedResultImages).list();
            marked = dir == null ? 0 : dir.length;
        }

    }

    public List<Match> getTemplateLocations(File templateFile, File imageFile) {
        GrayF32 template = UtilImageIO.loadImage(templateFile, true, SB_F32);
        GrayF32 image = UtilImageIO.loadImage(imageFile, true, SB_F32);

        List<Match> matches = findMatches(template, image).stream()
                .filter(m -> m.score > matchThreshold)
                .collect(Collectors.toList());

        if (pathMarkedResultImages != null) {
            storeMarkedResultImage(pathMarkedResultImages, template, image, matches);
        }
        return matches;
    }

    private void storeMarkedResultImage(String pathMarkedResultImages, GrayF32 template, GrayF32 image, List<Match> matches) {
        BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
        ConvertBufferedImage.convertTo(image, output);
        Graphics2D g2 = output.createGraphics();
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(5));
        drawRectangles(g2, template, matches);
        UtilImageIO.saveImage(output, pathMarkedResultImages + (++marked) + ".png");
    }

    private List<Match> findMatches(GrayF32 template, GrayF32 image) {
        TemplateMatching<GrayF32> matcher =
                FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_SQUARE_ERROR, GrayF32.class);

        matcher.setImage(image);
        matcher.setTemplate(template, null, 500);
        try {
            matcher.process();
        } catch (ArrayIndexOutOfBoundsException e) {
            return Collections.emptyList();
        }

        return matcher.getResults().toList();
    }

    private void drawRectangles(Graphics2D g2,
                                GrayF32 template, List<Match> found) {
        int r = 2;
        int w = template.width + 2 * r;
        int h = template.height + 2 * r;

        for (Match m : found) {
            // the return point is the template's top left corner
            int x0 = m.x - r;
            int y0 = m.y - r;
            int x1 = x0 + w;
            int y1 = y0 + h;

            g2.drawLine(x0, y0, x1, y0);
            g2.drawLine(x1, y0, x1, y1);
            g2.drawLine(x1, y1, x0, y1);
            g2.drawLine(x0, y1, x0, y0);
        }
    }
}
