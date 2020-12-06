package google.maps.webview;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JsBridge {

    private final Consumer<String> onUrl;
    private final BiConsumer<String, String> onResponse;

    public JsBridge(WebEngine webEngine, Consumer<String> onUrl, BiConsumer<String, String> onResponse) {
        this.onUrl = onUrl;
        this.onResponse = onResponse;
        // jsbridge must be a variable. see https://www.frankhissen.de/javafx-webview-webkit-fehler-javascript-java-kommunikation-java-8-frank-hissen-it-blog.html
        JsBridge jsbridge = this;
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                 Worker.State newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("jsbridge", jsbridge);
                        webEngine.executeScript(xmlrequestStartInterceptor);
                        webEngine.executeScript(xmlrequestDoneInterceptor);
                        cssTweaks.styleIds(webEngine, "vasquette", "minimap", "fineprint");
                        cssTweaks.styleClasses(webEngine, "app-viewcard-strip", "widget-pane-toggle-button-container");
                    }
                });
    }

    public void urlLoading(String method, String url) {
        onUrl.accept(url);
    }

    public void onResponse(String headers, String body) {
        onResponse.accept(headers, body);
    }

    private final String xmlrequestStartInterceptor = """
            var g_initial_loading_finished = false;
                        
            ((() => {
                        const open = XMLHttpRequest.prototype.open;            
                        XMLHttpRequest.prototype.open = function(method, url, ...rest) {
                            jsbridge.urlLoading(method, url);   
                            
                            g_initial_loading_finished = g_initial_loading_finished || url.indexOf('/maps/preview/merchantstatus') >= 0;                              
                            if((! g_initial_loading_finished) || url.indexOf('/maps/preview/place?') >= 0) {          
                                return open.call(this, method, url, ...rest);
                            } else {                              
                                return null;
                            };
                        };
                    }))();
                    """;

    private final String xmlrequestDoneInterceptor = """
            ((() => {
                        const origOpen = XMLHttpRequest.prototype.open;
                        XMLHttpRequest.prototype.open = function() {
                                this.addEventListener('load', function() {
                                    if(g_initial_loading_finished) {
                                        jsbridge.onResponse(this.getAllResponseHeaders(), this.responseText);
                                    }
                                });
                           origOpen.apply(this, arguments);
                        };
                    }))();
                    """;

}
