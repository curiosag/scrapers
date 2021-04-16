package google.maps.webview;

public enum ProcessingType {
    manual_search("TYPE_HINDU_TEMPLE"), marker_temple("TYPE_HINDU_TEMPLE"), marker_any("any");

    public String place_type;

    ProcessingType(String place_type) {
        this.place_type = place_type;
    }
}