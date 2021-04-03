package google.maps;

import java.util.Objects;

import static google.maps.webview.markers.RGB.*;

public class MarkerCoordinate {
    public final long x;
    public final long y;
    public final int color;

    public MarkerCoordinate(long x, long y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkerCoordinate that = (MarkerCoordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "PixelCoordinate{" +
                "x=" + x +
                ", y=" + y +
                ", rgb(" + red(color) + "/" + green(color) + "/" + blue(color) + ")"+
                '}';
    }
}
