package google.maps.tiles;

import java.util.Objects;

public record PixelSequence(long x, long y, int length, int color) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixelSequence that = (PixelSequence) o;
        return x == that.x && y == that.y && length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, length);
    }

}