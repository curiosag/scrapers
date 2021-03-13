package google.maps.tiles;

import google.maps.Point;

import java.util.function.Consumer;

public class RowAnalyzer {

    private final Consumer<Point> onPoint;

    public RowAnalyzer(Consumer<Point> onPoint) {
        this.onPoint = onPoint;
    }

}
