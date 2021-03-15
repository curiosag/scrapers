package google.maps.extraction;

import google.maps.Area;
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

import static google.maps.Area.rectangle;

public class Export {

    public static void main(String[] args) throws IOException {
        RegionsDao r = new RegionsDao();
// List<List<Point>> area = r.getBoundaries("name0", "Sri Lanka");

        Polygon p = asPolygon(new Area(rectangle(new Point(-2, 100), new Point(-10, 125))).getBoundary());
        //createCsv("/home/ssmertnig/temp/scrapemore/Bali.csv", p);
        createHtml("/home/ssmertnig/dev/repo/scrapers/src/main/resources/Nepal.html", p);
    }

    private static void createHtml(String pageFullName, Polygon poly) throws IOException {
        Export viz = new Export();
        PlacesDao p = new PlacesDao();

        try (FileWriter w = new FileWriter(pageFullName)) {
            List<PlaceSearchResultItem> places = p.getPlaces().stream().filter(i -> i.id > 19725).collect(Collectors.toList());// (List<PlaceSearchResultItem>) restrict(p.getPlaces(), poly);
            String rendered = viz.getPage(places, Collections.emptyList(), Collections.emptyList());
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
            List<PlaceSearchResultItem> places = (List<PlaceSearchResultItem>) restrict(p.getPlaces(), poly);
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

        String circles = IntStream.rangeClosed(0, circleCenters.size() - 1).boxed()
                .map(i -> String.format(" c%d:{ lat:  %.10f, lng: %.10f, radius: 5000 }", i, circleCenters.get(i).lat, circleCenters.get(i).lon))
                .collect(Collectors.joining(",\n"));

        String p = places.stream()
                .map(i -> String.format("{ lat: %.10f, lng: %.10f, name:\"%s\"}", i.getLatitude(), i.getLongitude(), String.format("%s, %s, %s, %s %.5f %.5f", i.name, i.plus_compound_code, i.vicinity, i.placeId, i.lat, i.lon)))
                .collect(Collectors.joining(",\n"));

        return page.replace("circleDataHere!", circles)
                .replace("polyCoordDataHere!", poly)
                .replace("placesHere!", p);
    }

    private final static String page = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Simple Map</title>
                <script src="https://polyfill.io/v3/polyfill.min.js?features=default"></script>
                <script
                        src="https://maps.googleapis.com/maps/api/js?key=API-KEY&callback=initMap&libraries=&v=weekly"
                        defer
                ></script>
                <style type="text/css">
                    #map {
                        height: 96%;
                    }
                        
                    html,
                    body {
                        height: 100%;
                        margin: 0;
                        padding: 0;
                    }
                </style>
                <script>
                    const polyCoords = [
                        polyCoordDataHere!
                    ];
                    
                 const locations = [
                     placesHere!
                 ]
                        
                    let map;
                        
                    const circles = {
                        circleDataHere!
                    };
                        
                    function initMap() {
                        map = new google.maps.Map(document.getElementById("map"), {
                            zoomControl: true   ,
                            scaleControl: true,
                            zoom: 8,
                            center: { lat: 9.9, lng: 79.6 },
                        });
                                                
                        function displayCoordinates(pnt) {
                                  var coordsLabel = document.getElementById("tdCursor");
                                  var lat = pnt.lat();
                                  lat = lat.toFixed(4);
                                  var lng = pnt.lng();
                                  lng = lng.toFixed(4);
                                  coordsLabel.innerHTML = "Latitude: " + lat + "  Longitude: " + lng;
                              }
                              
                        google.maps.event.addListener(map, 'mousemove', function (event) {
                             displayCoordinates(event.latLng);              
                        });      
                        
                        polyCoords.forEach(function (element, index) {
                            (new google.maps.Polygon({
                                paths: polyCoords[index],
                                strokeColor: "#FF0000",
                                strokeOpacity: 0.8,
                                strokeWeight: 1,
                                fillColor: "#FF0000",
                                fillOpacity: 0.0,
                            })).setMap(map);
                        });
                        
                        for (const c in circles) {
                            new google.maps.Circle({
                                strokeColor: "#ff0000",
                                strokeOpacity: 0.8,
                                strokeWeight: 1,
                                fillColor: "#FF0000",
                                fillOpacity: 0.0,
                                map,
                                center: circles[c],
                                radius: circles[c].radius,
                            });
                        }
                        
                        for (const i in locations) {
                            new google.maps.Marker({
                                position: locations[i],
                                map,
                                title: locations[i].name,
                            });
                        }
                    }
                        
                </script>
            </head>
            <body>
            <div id="map"></div>
            <div id="tdCursor"/>
            </body>
            </html>
            """;


}
