package google.maps.webview;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;

import java.util.Timer;
import java.util.TimerTask;

public class MoveTimerTask extends TimerTask {

    public final float x;
    public final float y;
    public final float startX;
    public final float endX;
    public final float startY;
    public final float endY;
    public final float deltaX;
    public final float deltaY;
    private final long delay;
    private final Robot robot;
    private final Runnable onMoved;
    private final Timer timer;

    /*
    Can't be done with a pre-calculated list like in GrazingTimerTask because the movement of the map is irregular
    * */
    public MoveTimerTask(float x, float startX, float endX, float deltaX, float y, float startY, float endY, float deltaY, long delay, Robot robot, Runnable onMoved, Timer timer) {
        this.x = x;
        this.y = y;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.delay = delay;
        this.robot = robot;
        this.onMoved = onMoved;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (Float.compare(x, startX) == 0 && Float.compare(y, startY) == 0) {
            Platform.runLater(() -> {
                robot.mouseMove(x, y);
                robot.mousePress(MouseButton.PRIMARY);
                scheduleNext();
            });
        } else if (between(x, startX, endX) && between(y, startY, endY)) {
            Platform.runLater(() -> {
                robot.mouseMove(x, y);
                scheduleNext();
            });
        } else {
            Platform.runLater(() -> {
                robot.mouseRelease(MouseButton.PRIMARY);
                onMoved.run();
            });
        }
    }

    private boolean between(float i, float rangeStart, float rangeEnd) {
        float start = rangeStart;
        float end = rangeEnd;
        if (rangeStart > rangeEnd) {
            start = rangeEnd;
            end = rangeStart;
        }
        assert (start < end);
        return start <= i && i <= end;
    }

    private void scheduleNext() {
        timer.schedule(new MoveTimerTask(x + deltaX, startX, endX, deltaX, y + deltaY, startY, endY, deltaY, delay, robot, onMoved, timer), delay);
    }

    @Override
    public String toString() {
        return "MoveTimerTask{" +
                "x=" + x +
                ", y=" + y +
                ", startX=" + startX +
                ", endX=" + endX +
                ", startY=" + startY +
                ", endY=" + endY +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                '}';
    }
}
