package google.maps.webview.intercept;

import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ResponseCollector {
    private final ConcurrentHashMap<Long, Response> responses = new ConcurrentHashMap<>();
    private final Consumer<Response> onResponseComplete;

    public ResponseCollector(Consumer<Response> onResponseComplete) {
        this.onResponseComplete = onResponseComplete;
    }

    public void onResponse(URLConnection c) {
        var tid = getThreadId();
        if (responses.containsKey(tid))
            throw new IllegalStateException();
        System.out.printf("Response on thread %d: %s",tid, c.getURL().toString());
        responses.put(tid, new Response(c));
    }

    public void onData(ByteBuffer buffer) {
        Response r = responses.get(getThreadId());
        if (r == null)
            throw new IllegalStateException();

        byte[] bytes = new byte[buffer.limit()];
        for (int i = 0; i < buffer.limit(); i++) {
            bytes[i] = buffer.get(i);
        }

        r.setData(bytes);
    }

    public void onComplete() {
        Response r = responses.get(getThreadId());
        if (r == null)
            throw new IllegalStateException();

        responses.remove(getThreadId());
        onResponseComplete.accept(r);
    }

    private long getThreadId() {
        return Thread.currentThread().getId();
    }
}
