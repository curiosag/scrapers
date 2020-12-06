package persistence;

public class ScrapeSlice {
    public final int id;
    public String dtFrom;
    public String dtTo;
    public int pagesDone;

    public boolean sliceDone;

    public ScrapeSlice(int id) {
        this.id = id;
    }

    public ScrapeSlice setDtFrom(String dtFrom) {
        this.dtFrom = dtFrom;
        return this;
    }

    public ScrapeSlice setDtTo(String dtTo) {
        this.dtTo = dtTo;
        return this;
    }

    public ScrapeSlice setPagesDone(int pagesDone) {
        this.pagesDone = pagesDone;
        return this;
    }

    public ScrapeSlice setSliceDone(boolean sliceDone) {
        this.sliceDone = sliceDone;
        return this;
    }
}
