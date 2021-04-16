package google.maps.webview;

import google.maps.Point;

import java.util.Deque;
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
            retouch((float) l.lat, (float) l.lon, 2);
        } else {
            onGrazed.run();
        }
    }

    private void retouch(float x, float y, int count) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (count != 0) {
                    touch.accept(x, y);
                    retouch(x, y - count * 7, count - 1);
                } else {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            touch.accept(200f, 3f); // ensure any popups disappear again by clicking on the window frame
                            timer.schedule(new GrazingTimerTask(locations, touch, delay, onGrazed, timer), Math.max(delay, 150));
                        }
                    }, 150);

                }
            }
        }, 160);
    }
}
