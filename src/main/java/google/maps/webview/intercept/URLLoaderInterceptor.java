package google.maps.webview.intercept;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class URLLoaderInterceptor {

    public static BiFunction<Object, URLConnection, Boolean> onSendRequest = (a,b) -> true;
    public static BiConsumer<Object, URLConnection> onDidReceiveResponse = (a,b) -> {
    };
    public static BiConsumer<Object, ByteBuffer> onDidReceiveData = (a,b) -> {
    };
    public static Consumer<Object> onFinishedLoading = o -> {
    };

    // https://stackoverflow.com/questions/39987414/delegate-private-method-in-bytebuddy-with-super-possible
    public static void didFinishLoading(@This Object tis, @SuperCall Callable<Void> c) throws Exception {
        if (onFinishedLoading == null)
            throw new IllegalStateException();
        onFinishedLoading.accept(tis);

        c.call();
    }

    public static void sendRequest(@This Object tis, @SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onSendRequest == null|| args.length < 1|| !(args[0] instanceof URLConnection))
            throw new IllegalStateException("smthgs wrong");

        URLConnection _this = (URLConnection) args[0];
        if (onSendRequest.apply(tis, _this)) {
            c.call();
        } else {
            _this.getInputStream().close();// close connection
            throw new RuntimeException("Nix request...");
        }
    }

    public static void didReceiveData(@This Object tis, @SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof ByteBuffer))
            throw new IllegalStateException();
        onDidReceiveData.accept(tis, (ByteBuffer) args[0]);

        c.call();
    }

    public static void didReceiveResponse(@This Object tis, @SuperCall Callable<Void> c, @AllArguments Object[] args) throws Exception {
        if (onDidReceiveData == null || args.length < 1 || !(args[0] instanceof URLConnection))
            throw new IllegalStateException();
        onDidReceiveResponse.accept(tis, (URLConnection) args[0]);

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

    }

}
