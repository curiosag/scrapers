package google.maps;

import java.util.Objects;

public class PixelCoordinate {
    public final long x;
    public final long y;

    public PixelCoordinate(long x, long y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixelCoordinate that = (PixelCoordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
