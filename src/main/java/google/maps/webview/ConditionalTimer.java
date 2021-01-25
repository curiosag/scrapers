package google.maps.webview;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class ConditionalTimer {

    final Supplier<Boolean> canSchedule;
    Timer timer;

    public ConditionalTimer(Supplier<Boolean> canSchedule, String name, boolean isDaemon) {
        timer = new Timer(name, isDaemon);
        this.canSchedule = canSchedule;
    }

    public void schedule(TimerTask task, long delay) {
        if (canSchedule.get())
            timer.schedule(task, delay);
    }

}
