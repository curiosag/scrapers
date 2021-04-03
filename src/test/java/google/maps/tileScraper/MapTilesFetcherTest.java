package google.maps.tileScraper;

import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static google.maps.IOUtil.storeImage;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class MapTilesFetcherTest {

    @Test
    public void fetchTiles() {
        MapTilesFetcher fetcher = new MapTilesFetcher(5, 15, 4, new HttpTileFetcher());
        Optional<Tile[][]> tiles = fetcher.fetchTiles(23548, 15209);
        tiles.ifPresent(this::dump);
    }

    private void dump(Tile[][] t) {
        for (int y = 0; y < t.length; y++) {
            for (int x = 0; x < t[y].length; x++) {
                storeImage(t[y][x].getImage(), String.format("/home/ssmertnig/temp/tiles/t_%d_%d.png", y, x));
            }
        }
    }

    /*
    *  caching is build to serve requests for squares overlapping by 1 tile
    * */
    @Test
    public void testCaching() {
        SingleTileFetcher mockTileFetcher = new SingleTileFetcher() {
            @Override
            public Tile fetchTile(int x, int y, int zoom) {
                return new Tile(x, y, zoom, null);
            }
        };

        int squareSize = 10;
        MapTilesFetcher fetcher = new MapTilesFetcher(squareSize, 15, 4, mockTileFetcher);

        Tile[][] tiles_0_0 = fetcher.fetchTiles(0, 0).get();
        String expected_0_0 = """
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                1 1 1 1 1 1 1 1 1 3\s
                """;

        /*
         *   19 0
         *    0 0
         * */

        assertEquals(expected_0_0, getAccessMatrix(tiles_0_0));
        assertEquals(9 + 10, countCached(fetcher));

        Tile[][] tiles_0_1 = fetcher.fetchTiles(9, 0).get();
        String expected_0_1 = """
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                2 1 1 1 1 1 1 1 1 3\s
                """;

        /*
         *   10 18
         *   0  0
         * */

        assertEquals(expected_0_1, getAccessMatrix(tiles_0_1));
        assertEquals(10 + 18, countCached(fetcher));

        Tile[][] tiles_1_0 = fetcher.fetchTiles(0, 9).get();
        String expected_1_0 = """
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                1 1 1 1 1 1 1 1 1 3\s
                """;

        /*
         *   1 18
         *  18  0
         * */
        assertEquals(expected_1_0, getAccessMatrix(tiles_1_0));
        assertEquals(1 + 18 + 18, countCached(fetcher));

        Tile[][] tiles_1_1 = fetcher.fetchTiles(9, 9).get();
        System.out.println(getAccessMatrix(tiles_1_1));

        String expected_1_1 = """
                0 0 0 0 0 0 0 0 0 2\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                0 0 0 0 0 0 0 0 0 1\s
                2 1 1 1 1 1 1 1 1 3\s
                """;
        /*
         *   0 9
         *  9 9+9+1
         * */

        assertEquals(expected_1_1, getAccessMatrix(tiles_1_1));
        assertEquals(4 * 9 + 1, countCached(fetcher));
    }

    private int countCached(MapTilesFetcher f) {
        return f.tileCache.values().stream()
                .map(Map::size)
                .reduce(0, Integer::sum);
    }

    private String getAccessMatrix(Tile[][] tiles) {
        StringBuilder sb = new StringBuilder();
        for (Tile[] ti : tiles) {
            for (Tile t : ti) {
                sb.append(t.getExpectedAccesses() - t.getAccessCount());
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Test
    public void calculateExpectedAccesses() {
    }

}