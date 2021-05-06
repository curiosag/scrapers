package google.maps.tileScraper;

import java.util.Objects;

public class TileCoordinate {

    public final int x;
    public final int y;
    public final int zoom;

    public TileCoordinate(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileCoordinate that = (TileCoordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "TileCoordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
