package google.maps.webview;

import google.maps.Point;

import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;

public class GrazingTimerTask extends TimerTask {
    private final BiConsumer<Float, Float> touch;
    private final long delay;
    private final Runnable onGrazed;
    private final Deque<Point> locations;
    private final ConditionalTimer timer;

    public GrazingTimerTask(Deque<Point> locations, BiConsumer<Float, Float> touch, long delay, Runnable onGrazed, ConditionalTimer timer) {
        this.touch = touch;
        this.delay = delay;
        this.onGrazed = onGrazed;
        this.locations = locations;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (locations.size() > 0) {
            Point l = locations.pop();
            touch.accept((float) l.lat, (float) l.lon);
            timer.schedule(new GrazingTimerTask(locations, touch, delay, onGrazed,timer), delay);
        } else {
            onGrazed.run();
        }
    }
}
