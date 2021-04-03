package google.maps.tileScraper;

public interface CachedItem<T> {

    public T setExpectedAccesses(int expected);

    public int getExpectedAccesses();

    public int getAccessCount();

    public void countAccess();

}
