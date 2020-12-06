package google.maps;

import google.maps.dao.PlacesDao;
import google.maps.dao.RegionsDao;
import google.maps.extraction.ResultFileExtractor;
import google.maps.searchapi.Locatable;
import google.maps.searchapi.PlaceSearchResultItem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Visualization {
    private static final String pageFullName = "/home/ssmertnig/dev/repo/scrapers/src/main/resources/TiruResults.html";

    public static void main(String[] args) throws IOException {
        createCsv("/home/ssmertnig/temp/scrapemore/tiruvannamalai.csv");
    }

    private static void createHtml() throws IOException {
        Visualization viz = new Visualization();
        RegionsDao r = new RegionsDao();
        PlacesDao p = new PlacesDao();
        List<List<Point>> area = r.getBoundaries("name2", "Tiruvannamalai");

        try (FileWriter w = new FileWriter(pageFullName)) {
            List<PlaceSearchResultItem> places = (List<PlaceSearchResultItem>) restrict(p.getPlaces(), "name2", "Tiruvannamalai");
            String rendered = viz.getPage(places, Collections.emptyList(), area);
            w.write(rendered);
        }
    }

    private static void createCsv(String csvFileName) throws IOException {
        PlacesDao p = new PlacesDao();

        try (FileWriter w = new FileWriter(csvFileName)) {
            List<PlaceSearchResultItem> places = (List<PlaceSearchResultItem>) restrict(p.getPlaces(), "name2", "Tiruvannamalai");
            places.forEach(i -> {
                try {
                    w.write(ResultFileExtractor.getCsv(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static String strip(String part) {
        return part.replace("\"", "");
    }

    public static List restrict(List<? extends Locatable> points, String regionNameField, String regionName) {
        RegionsDao d = new RegionsDao();

        GeometryFactory geoFactory = new GeometryFactory();
        Coordinate[] coordinates = d.getStateBoundaries(regionNameField, regionName).stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);
        Polygon geo = geoFactory.createPolygon(coordinates);

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
                .map(i -> String.format("{ lat: %.10f, lng: %.10f, name:\"%s\"}", i.getLatitude(), i.getLongitude(), String.format("%s, %s, %s, %s", i.name, i.plus_compound_code, i.vicinity, i.placeId)))
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
                        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC-9YPM9emNZ7KFRMZQuHpgV9LJGaBpSt0&callback=initMap&libraries=&v=weekly"
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
