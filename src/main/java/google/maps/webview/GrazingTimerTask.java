package google.maps.webview;

import google.maps.Point;

import java.util.Deque;
import java.util.TimerTask;
import java.util.function.BiConsumer;

public class GrazingTimerTask extends TimerTask {
    private final float windowX;
    private final float windowY;
    private final RequestSequencer sequencer;
    private final BiConsumer<Float, Float> touch;
    private final long delay;
    private final Runnable onGrazed;
    private final Deque<Point> locations;
    private final ConditionalTimer timer;

    public GrazingTimerTask(float windowX, float windowY, Deque<Point> locations, BiConsumer<Float, Float> touch, long delay, Runnable onGrazed, ConditionalTimer timer, RequestSequencer sequencer) {
        this.windowX = windowX;
        this.windowY = windowY;
        this.sequencer = sequencer;
        this.touch = touch;
        this.delay = delay;
        this.onGrazed = onGrazed;
        this.locations = locations;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (locations.size() == 0) {
            onGrazed.run();
            return;
        }

        Point l = locations.pop();
        if (locations.size() > 0)
            sequencer.awaitRequestResponse(() -> timer.schedule(new GrazingTimerTask(windowX, windowY, locations, touch, delay, onGrazed, timer, sequencer), delay));
        else
            sequencer.awaitRequestResponse(onGrazed);
        // the response awaited above gets triggered below. Only after these clicks yielded a response the next location gets grazed

        float lat = (float) l.lat;
        float lon = (float) l.lon;
        touch.accept(lat, lon);
        for (int i = 0; i < 7; i++) { // cover some area
            scheduleTouch(lat + 1, lon + i, 10);
        }
        // move away from marker to prevent info popup. once the cursor runs into the popup maps won't issue requests any more
        scheduleTouch(windowX + 100f, windowY + 3f, 6 * 10 + 500);
    }

    private void scheduleTouch(float lat, float lon, int delay) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                touch.accept(lat, lon);
            }
        }, delay);
    }

    private void sleep() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
        }
    }
}
