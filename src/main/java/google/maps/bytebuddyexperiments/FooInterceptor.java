package google.maps.bytebuddyexperiments;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.util.concurrent.Callable;


public class FooInterceptor {

    // https://stackoverflow.com/questions/39987414/delegate-private-method-in-bytebuddy-with-super-possible
    public static void foo(@SuperCall Callable<Void> c) throws Exception
    {
        c.call();
    }

    public static int getNum(@SuperCall Callable<Integer> c, @AllArguments Object[] args) throws Exception {
        return c.call();
    }
}
