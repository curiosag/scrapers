package google.maps.webview;

import google.maps.webview.intercept.URLLoaderInterceptor;

import java.lang.instrument.Instrumentation;

public class Launcher {

    public static void main(String[] args) throws Exception {
        ScraperApplication.main(args);
    }

    // to trigger pass jvm parameter: -javaagent:<path to byte-buddy-agent.jar>
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        URLLoaderInterceptor.setup(instrumentation);
    }

}
