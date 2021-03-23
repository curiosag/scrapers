package google.maps.tiles;

import google.maps.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TileScrapeFileWriter implements TileScrapeWriter {
    private final String destFile ;
    private final String savePointFile;

    public TileScrapeFileWriter(String destFile) {
        this.destFile = destFile;
        this.savePointFile = destFile + ".safepoint";
    }

    @Override
    public void write(List<Point> markerCoordinates, int savePointX, int savePointY) {

        try (FileWriter w = new FileWriter(destFile, StandardCharsets.UTF_8, true)) {
            markerCoordinates.forEach(i -> {
                try {
                    w.write(String.format("insert into lola_place_fastscan(geom) values(public.st_geomfromtext('POINT(%.7f %.7f)', 4326));\n", i.lon, i.lat));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeSafePoint(savePointX, savePointY);
    }

    void writeSafePoint(int x, int y) {
        try (FileWriter w = new FileWriter(savePointFile, StandardCharsets.US_ASCII, false)) {
            w.write(Integer.valueOf(x).toString());
            w.write(';');
            w.write(Integer.valueOf(y).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TileCoordinate> getSafePoint() {
        if (! Files.exists(Path.of(savePointFile)))
            return Optional.empty();

        Scanner scanner = new Scanner(savePointFile);
        scanner.useDelimiter(";");
        return Optional.of(new TileCoordinate(scanner.nextInt(), scanner.nextInt()));
    }

}
