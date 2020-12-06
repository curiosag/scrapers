package google.maps.searchapi;

public class PlaceSearchResultItem implements Locatable {
    public final String placeId;
    public final double lat;
    public final double lon;
    public final String name;
    public final String plus_compound_code;
    public final String adress;
    public final String vicinity;

    public PlaceSearchResultItem(String placeId, double lat, double lon, String name, String plus_compound_code, String adress, String vicinity) {
        this.placeId = placeId;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.plus_compound_code = plus_compound_code;
        this.adress = adress;
        this.vicinity = vicinity;
    }

    @Override
    public double getLatitude() {
        return lat;
    }

    @Override
    public double getLongitude() {
        return lon;
    }
}
