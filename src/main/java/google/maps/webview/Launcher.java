package google.maps.webview;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

import static google.maps.webview.Log.log;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Launcher {

    static Instrumentation instrumentation;

    public static void main(String[] args) throws Exception {
        setupInterceptor();
        ScraperApplication.main(args);
    }

    private static void setupInterceptor() {
        // obtain the class loader
        // don't access the intercepted class before ByteBuddy can get its hand on it. The jvm for good reasons will give you a
        // UnsupportedOperationException: class redefinition failed: attempted to add a method
        // https://stackoverflow.com/questions/40774684/error-while-redefining-a-method-with-bytebuddy-class-redefinition-failed-atte
        ClassLoader interceptorClassLoader = TrivialCookieStore.class.getClassLoader();
        ClassLoader urlloaderClassLoader = com.sun.webkit.network.CookieManager.class.getClassLoader();

        TypePool interceptorTypepool = TypePool.Default.of(interceptorClassLoader);
        TypePool urlloaderTypepool = TypePool.Default.of(urlloaderClassLoader);

        TypeDescription fooType = urlloaderTypepool.describe("com.sun.webkit.network.URLLoader").resolve();
        TypeDescription URLLoaderInterceptorType = interceptorTypepool.describe("google.maps.webview.URLLoaderInterceptor").resolve();

        new ByteBuddy().rebase(fooType, ClassFileLocator.ForClassLoader.of(interceptorClassLoader))
                .method(named("didReceiveData")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didFinishLoading")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didReceiveResponse")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .make()
                .load(urlloaderClassLoader, ClassReloadingStrategy.of(instrumentation));

        System.out.println("hu!");

        URLLoaderInterceptor.onDidReceiveData = (data) -> {
            log(String.format("Tid %d onDidRecieveData", Thread.currentThread().getId()));
        };
        URLLoaderInterceptor.onFinishedLoading = () -> {
            log(String.format("Tid %d onFinishedLoading", Thread.currentThread().getId()));
        };
        URLLoaderInterceptor.onDidReceiveResponse = (r) -> {
            log(String.format("Tid %d onDidReceiveResponse for %s", Thread.currentThread().getId(), r.getURL()));
        };
    }


    // to trigger pass jvm parameter: -javaagent:<path to byte-buddy-agent.jar>
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        Launcher.instrumentation = instrumentation;
    }
}
