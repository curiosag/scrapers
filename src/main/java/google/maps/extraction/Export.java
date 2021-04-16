package google.maps.extraction;

import google.maps.Point;
import google.maps.dao.PlacesDao;
import google.maps.dao.RegionsDao;
import google.maps.searchapi.Locatable;
import google.maps.searchapi.PlaceSearchResultItem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Export {

    private static final String baseQuery = """
                select p.id, p.place_id, p.name, p.address, p.global_code, p.vicinity, ST_AsText(p.geom) as geom
                from temple.temple.place_scraped p\040
                """;
    private static final String regionJoin = " join temple.temple.region r on r.id = %d and st_within(p.geom, r.geom)";

    public static void main(String[] args) throws IOException {
//        RegionsDao r = new RegionsDao();
// List<List<Point>> area = r.getBoundaries("name0", "Sri Lanka");

        //    Polygon p = asPolygon(new Area(rectangle(new Point(-2, 100), new Point(-10, 125))).getBoundary());
        //createCsv("/home/ssmertnig/temp/scrapemore/Bali.csv", p);
        String query = baseQuery + "where st_y(geom) < 0";
        createHtml("/home/ssmertnig/dev/repo/scrapers/src/main/resources/south.html", query,"AIzaSyA_TRJKtGl2rC9sQ4Kv5Dl6XbAUkL3JS8w");
    }

    private static void createClusterHtml(String pageFullName, String apiKey) throws IOException {
        Export viz = new Export();
        List<List<Point>> clusters = new ClusterDao().getClusters().stream().map(ClusterDao.Cluster::area).collect(Collectors.toList());
        try (FileWriter w = new FileWriter(pageFullName)) {
            String rendered = viz.getPage(Collections.emptyList(), Collections.emptyList(), clusters).replace("API-KEY", apiKey);
            w.write(rendered);
        }
    }

    private static void createHtml(String pageFullName, String query, String apiKey) throws IOException {
        Export viz = new Export();
        PlacesDao p = new PlacesDao();

        try (FileWriter w = new FileWriter(pageFullName)) {
            List<PlaceSearchResultItem> places = p.getQueryResult(query);// (List<PlaceSearchResultItem>) restrict(p.getPlaces(), poly);
            String rendered = viz.getPage(places, Collections.emptyList(), Collections.emptyList()).replace("API-KEY", apiKey);
            w.write(rendered);
        }
    }

    private static Polygon getCountryBoundary(String countryName){
        RegionsDao d = new RegionsDao();
        return asPolygon(d.getStateBoundaries("name0", countryName));
    }

    private static Polygon asPolygon(List<Point> points){
        GeometryFactory geoFactory = new GeometryFactory();
        Coordinate[] coordinates = points.stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);
        return geoFactory.createPolygon(coordinates);
    }

    private static Polygon getStateBoundary(String stateName){
        RegionsDao d = new RegionsDao();

        GeometryFactory geoFactory = new GeometryFactory();
        Coordinate[] coordinates = d.getStateBoundaries("name2", stateName).stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);
        return geoFactory.createPolygon(coordinates);
    }

    private static void createCsv(String csvFileName, Polygon poly) throws IOException {
        PlacesDao p = new PlacesDao();

        try (FileWriter w = new FileWriter(csvFileName)) {
            List<PlaceSearchResultItem> places = (List<PlaceSearchResultItem>) restrict(p.getPlaces(50), poly);
            places.forEach(i -> {
                try {
                    w.write(ResultFileExtractor.getCsv(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static List restrict(List<? extends Locatable> points, Polygon geo) {
        RegionsDao d = new RegionsDao();

        GeometryFactory geoFactory = new GeometryFactory();
        return points.stream()
                .filter(p -> geo.contains(new org.locationtech.jts.geom.Point(new Coordinate(p.getLatitude(), p.getLongitude()), geoFactory.getPrecisionModel(), geoFactory.getSRID())))
                .collect(Collectors.toList());

    }

    public String getPage(List<PlaceSearchResultItem> places, List<Point> circleCenters, List<List<Point>> polyCoords) {
        String poly = polyCoords.stream()
                .map(c ->
                        "[ " + c.stream()
                                .map(p -> String.format("{ lat: %.10f, lng: %.10f }", p.lat, p.lon))
                                .collect(Collectors.joining(",\n")) + " ]"
                )
                .collect(Collectors.joining(",\n"));

        if(!(circleCenters.isEmpty() && polyCoords.isEmpty()))
            throw new IllegalStateException("Implementation with openlayers needs adaption!");

        String circles = IntStream.rangeClosed(0, circleCenters.size() - 1).boxed()
                .map(i -> String.format(" c%d:{ lat:  %.10f, lng: %.10f, radius: 5000 }", i, circleCenters.get(i).lat, circleCenters.get(i).lon))
                .collect(Collectors.joining(",\n"));

        String p = places.stream()
                .map(i -> String.format("add(%.10f, %.10f);",  i.getLongitude(), i.getLatitude()))
                .collect(Collectors.joining("\n"));

        return page.replace("circleDataHere!", circles)
                .replace("polyCoordDataHere!", poly)
                .replace("placesHere!", p);
    }

    private final static String page = """
            <!DOCTYPE html>
             <html lang="en">
             <head>
                 <meta charset="utf-8">
                 <title>Ooohoooooooooooooooooooooooommmmmmmm Namaaaaaaaaaaaaaaah Shivaaaaaaaaayaaaaaaaaaaaaaaaaahhhhhhhh</title>
                 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                 <link rel="stylesheet" href="https://openlayers.org/en/v4.6.5/css/ol.css" type="text/css">
                 <script src="https://openlayers.org/en/v4.6.5/build/ol.js" type="text/javascript"></script>

               <script>
                 var map;
                 var mapLat = 27.0;
                     var mapLng = 73.0;
                 var mapDefaultZoom = 6;

                 function initialize_map() {
                   map = new ol.Map({
                     target: "map",
                     layers: [
                         new ol.layer.Tile({
                             source: new ol.source.OSM({
                                   url: "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
                             })
                         })
                     ],
                     view: new ol.View({
                         center: ol.proj.fromLonLat([mapLng, mapLat]),
                         zoom: mapDefaultZoom
                     })
                   });
                 }

                 function add(lat, lng) {
                   var vectorLayer = new ol.layer.Vector({
                     source:new ol.source.Vector({
                       features: [new ol.Feature({
                             geometry: new ol.geom.Point(ol.proj.transform([parseFloat(lng), parseFloat(lat)], 'EPSG:4326', 'EPSG:3857')),
                         })]
                     }),
                     style: new ol.style.Style({
                       image: new ol.style.Icon({
                         anchor: [0.5, 0.5],
                         anchorXUnits: "fraction",
                         anchorYUnits: "fraction",
                         src: "https://upload.wikimedia.org/wikipedia/commons/e/ec/RedDot.svg"
                       })
                     })
                   });

                   map.addLayer(vectorLayer);\s
                 }


               </script>
             </head>
             <body onload="initialize_map();\s
             placesHere!
             ">
               <div id="map" style="width: 100vw; height: 100vh;"></div>
             hallo
             </body>
             </html>
            """;


}
