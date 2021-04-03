package google.maps.tileScraper;

public interface SingleTileFetcher {
    Tile fetchTile(int x, int y, int zoom);
}
