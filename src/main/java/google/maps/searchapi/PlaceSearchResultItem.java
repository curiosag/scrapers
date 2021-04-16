package google.maps.searchapi;

public class PlaceSearchResultItem implements Locatable {
    public Long id;
    public final String placeId;
    public final double lat;
    public final double lon;
    public final String name;
    public final String global_code;
    public final String adress;
    public final String vicinity;
    public String resultType = "null";

    public void setId(Long id) {
        this.id = id;
    }

    public PlaceSearchResultItem(Long id, String placeId, double lat, double lon, String name, String global_code, String adress, String vicinity) {
        this.id = id;
        this.placeId = placeId;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.global_code = global_code;
        this.adress = adress;
        this.vicinity = vicinity;
    }

    public PlaceSearchResultItem(String placeId, double lat, double lon, String name, String global_code, String adress, String vicinity) {
        this.placeId = placeId;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.global_code = global_code;
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

    @Override
    public String toString() {
        return "PlaceSearchResultItem{" +
                "id=" + id +
                ", placeId='" + placeId + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                ", global_code='" + global_code + '\'' +
                ", adress='" + adress + '\'' +
                ", vicinity='" + vicinity + '\'' +
                ", resultType='" + resultType + '\'' +
                '}';
    }
}
