package google.maps.webview.cs_remote;

public class ReleaseJobDto {
    public int id;
    public boolean done;
    public String error;

    public ReleaseJobDto(int id, boolean done, String error) {
        this.id = id;
        this.done = done;
        this.error = error;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
