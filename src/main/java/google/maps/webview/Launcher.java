package google.maps.webview;

import google.maps.webview.intercept.Response;
import google.maps.webview.intercept.ResponseCollector;
import google.maps.webview.intercept.URLLoaderInterceptor;
import google.maps.webview.intercept.XORCypher;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static google.maps.webview.Log.log;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Launcher {

    static Instrumentation instrumentation;
    static ResponseCollector collector = new ResponseCollector(Launcher::onResponseCollected);

    public static void main(String[] args) throws Exception {
        setupInterceptor();
        ScraperApplication.main(args);
    }

    private static void setupInterceptor() {
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
                .method(named("didReceiveData")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didFinishLoading")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .method(named("didReceiveResponse")).intercept(MethodDelegation.to(URLLoaderInterceptorType))
                .make()
                .load(urlloaderClassLoader, ClassReloadingStrategy.of(instrumentation));

        URLLoaderInterceptor.onDidReceiveData = (data) -> {
            collector.onData(data);
        };
        URLLoaderInterceptor.onFinishedLoading = () -> {
            collector.onComplete();
        };
        URLLoaderInterceptor.onDidReceiveResponse = (c) -> {
            collector.onResponse(c);
        };
    }

    private static void onResponseCollected(Response r) {
        try (FileWriter w = new FileWriter("responses.txt", StandardCharsets.UTF_8, true)) {
            String url = r.urlConnection.getURL().toString();
            w.write("\n----------------------------------------------------------------------------------------\n");
            w.write(url + '\n');
            if (r.data != null && r.data.length > 0) {
                String body = new String(r.data, StandardCharsets.UTF_8);
                if (startsWith(r.data, "XHR1"))
                    w.write(body);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isPng(byte[] b) {
        return b != null && b.length > 5 && b[0] == -119 && b[1] == 80 && b[2] == 78 && b[3] == 71;
    }

    private static boolean startsWith(byte[] data, String s){
        for (int i = 0; i < s.length(); i++) {
            if(data[i] != s.charAt(i))
                return false;
        }
        return true;
    }

    private static void decode(byte[] arr, FileWriter w) throws IOException {
        w.write("\n\nxor by nothing: " + ": " + new String(arr));
        for (int i = 0; i < 255; i++) {
            byte[] xored = XORCypher.encryptSingle(arr, i);
            w.write("\n\nxor by " + i + ": " + new String(xored));
        }
        System.out.println("\n\n!");
        System.exit(0);
    }

    // to trigger pass jvm parameter: -javaagent:<path to byte-buddy-agent.jar>
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        Launcher.instrumentation = instrumentation;
    }

    private void writeStrings(List<String> values) {
    }

}
