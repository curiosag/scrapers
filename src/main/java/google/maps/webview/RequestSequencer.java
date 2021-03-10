package google.maps.webview;

import com.google.common.base.Charsets;

import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

public class RequestSequencer {
    Runnable onResponse;
    String searchTerm;

    // looks like not all map markers cause requests after markers have been moved over by mouse
    // or for whatever reason no response ever may come
    // one is duplicate klick on the same marker after moving the map
    Timer deatlockBreaker;

    public synchronized void awaitRequestResponse(Runnable onResponse) {

        try {
            System.out.println("sequencer awaitRequestResponse");
            if (this.onResponse != null) {
                System.out.println("awaitRequestResponse. ouch, this.onResponse != null");
                throw new IllegalStateException();
            }
            deatlockBreaker = new Timer();
            deatlockBreaker.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (this) {
                        if (deatlockBreaker != null) {
                            try {
                                System.out.println("sequencer timeout");
                                cancelDeadlockBreaker();

                                searchTerm = null;
                                onResponse.run();
                                clearCallbackOnCancelDeadlockBreaker();
                            } catch (Exception e) {
                                e.printStackTrace();
                                reset();
                            }
                        }
                    }
                }

            }, 10000);

            this.onResponse = onResponse;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            reset();
        }
    }

    private void clearCallbackOnCancelDeadlockBreaker() {
        onResponse = null;
    }

    private static final int justSomeLongString = 20;

    public synchronized void onRequest(String url) {
        try {
            System.out.println("sequencer onrequest: " + url.substring(0, 30));
            if (searchTerm != null) {
                System.out.println("onRequest. ouch, searchTerm != null");
                throw new IllegalStateException();
            }

            if (url.startsWith("/maps/preview/place")) {
                int idxFrom = url.indexOf("&q=") + 3;

                if (idxFrom < justSomeLongString)
                    throw new IllegalStateException();

                String searchTerm = URLDecoder.decode(url, Charsets.UTF_8).substring(idxFrom).replace("&pf=t", "");

                System.out.println("Searchterm requested: " + searchTerm);

                this.searchTerm = searchTerm;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            reset();
        }
    }

    private void cancelDeadlockBreaker() {
        synchronized (this) {
            try {
                if (deatlockBreaker != null) {
                    deatlockBreaker.cancel();
                    deatlockBreaker.purge();
                    deatlockBreaker = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                reset();
            }
        }
    }

    private void reset() {
        if (this.onResponse != null) {
            onResponse.run();
        }
        searchTerm = null;
        cancelDeadlockBreaker();
    }

    public synchronized void onResponse(String response) {
        try {
            System.out.println("sequencer onResponse");
            if (searchTerm == null) {
                System.out.println("onResponse. ouch, searchTerm == null");
                throw new IllegalStateException();
            }
            if (response.contains(searchTerm)) {
                System.out.println("Searchterm response: " + searchTerm);

                if (this.onResponse != null) {
                    onResponse.run(); // may have been triggered already in deadlock breaker
                }
                cancelDeadlockBreaker();
                onResponse = null;
                searchTerm = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            reset();
        }
    }
}

