package google.maps.tiles;

public interface SingleTileFetcher {
    Tile fetchTile(int x, int y, int zoom);
}
