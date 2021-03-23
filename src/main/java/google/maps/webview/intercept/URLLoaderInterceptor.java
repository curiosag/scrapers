package google.maps.webview.intercept;

import google.maps.webview.PlaceDetailsWriter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class URLLoaderInterceptor {

    static Function<URLConnection, Boolean> onSendRequest;
    static Consumer<URLConnection> onDidReceiveResponse;
    static Consumer<ByteBuffer> onDidReceiveData;
    static Runnable onFinishedLoading;
    private static PlaceDetailsWriter loader = new PlaceDetailsWriter();

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
        URLConnection _this = (URLConnection) args[0];
        if (onSendRequest.apply(_this)) {
            c.call();
        } else {
            _this.getInputStream().close();// close connection
            throw new RuntimeException("Nix request Oida");
        }
    }

    public static void didReceiveData(@SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof ByteBuffer))
            throw new IllegalStateException();
        onDidReceiveData.accept((ByteBuffer) args[0]);

        c.call();
    }

    public static void didReceiveResponse(@SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof URLConnection))
            throw new IllegalStateException();
        onDidReceiveResponse.accept((URLConnection) args[0]);

        c.call();
    }

    public static void setup(Instrumentation instrumentation) {
        // obtain the class loader
        // don't access the intercepted class before ByteBuddy can get its hand on it. The jvm for good reasons will give you a
        // UnsupportedOperationException: class redefinition failed: attempted to add a method
        // https://stackoverflow.com/questions/40774684/error-while-redefining-a-method-with-bytebuddy-class-redefinition-failed-atte

        ClassLoader interceptorClassLoader = Response.class.getClassLoader();
        ClassLoader urlloaderClassLoader = com.sun.webkit.network.CookieManager.class.getClassLoader();

        TypePool interceptorTypepool = TypePool.Default.of(interceptorClassLoader);
        TypePool urlloaderTypepool = TypePool.Default.of(urlloaderClassLoader);

        TypeDescription fooType = urlloaderTypepool.describe("com.sun.webkit.network.URLLoader").resolve();
        TypeDescription URLLoaderInterceptorType = interceptorTypepool.describe("google.maps.webview.intercept.URLLoaderInterceptor").resolve();

        new ByteBuddy().rebase(fooType, ClassFileLocator.ForClassLoader.of(interceptorClassLoader))
                .method(named("sendRequest")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didReceiveData")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didFinishLoading")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didReceiveResponse")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .make()
                .load(urlloaderClassLoader, ClassReloadingStrategy.of(instrumentation));

        onSendRequest = (c) -> {
            if (c.getURL().toString().contains("/maps/preview/place")) {
                loader.put(c.getURL().toString());
                return false;
            }
            return true;
        };

        onDidReceiveData = (data) -> {
            //collector.onData(data);
        };
        onFinishedLoading = () -> {
            //collector.onComplete();
        };
        onDidReceiveResponse = (c) -> {
            //collector.onResponse(c);
        };
    }

}
