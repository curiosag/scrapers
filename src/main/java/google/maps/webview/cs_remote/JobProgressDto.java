package google.maps.webview.cs_remote;

public class JobProgressDto {
    private int id;
    private double currLon;
    private double currLat;
    private double pctDone;

    public JobProgressDto(int id, double currLon, double currLat, double pctDone) {
        this.id = id;
        this.currLon = currLon;
        this.currLat = currLat;
        this.pctDone = pctDone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCurrLon() {
        return currLon;
    }

    public void setCurrLon(double currLon) {
        this.currLon = currLon;
    }

    public double getCurrLat() {
        return currLat;
    }

    public void setCurrLat(double currLat) {
        this.currLat = currLat;
    }

    public double getPctDone() {
        return pctDone;
    }

    public void setPctDone(double pctDone) {
        this.pctDone = pctDone;
    }
}
