package google.maps.webview.intercept;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class URLLoaderInterceptor {

    public static Consumer<URLConnection> onSendRequest;
    public static Consumer<URLConnection> onDidReceiveResponse;
    public static Consumer<ByteBuffer> onDidReceiveData;
    public static Runnable onFinishedLoading;

    // https://stackoverflow.com/questions/39987414/delegate-private-method-in-bytebuddy-with-super-possible
    public static void didFinishLoading(@SuperCall Callable<Void> c) throws Exception {
        if (onFinishedLoading == null)
            throw new IllegalStateException();
        onFinishedLoading.run();

        c.call();
    }

    public static void sendRequest(@SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof URLConnection))
            throw new IllegalStateException();
        onSendRequest.accept((URLConnection) args[0]);

        c.call();
    }

    public static void didReceiveData(@SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof ByteBuffer))
            throw new IllegalStateException();
        onDidReceiveData.accept((ByteBuffer) args[0]);

        c.call();
    }

    public static void didReceiveResponse(@SuperCall Callable<Void> c, @AllArguments Object[] args)throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof URLConnection))
            throw new IllegalStateException();
        onDidReceiveResponse.accept((URLConnection) args[0]);

        c.call();
    }

}
