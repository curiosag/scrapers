package google.maps.webview;

public enum MarkerProcessingType {
    temple("hindu_temple"), any("any");

    public String place_type;

    MarkerProcessingType(String place_type) {
        this.place_type = place_type;
    }
}