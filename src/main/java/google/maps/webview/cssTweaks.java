package google.maps.webview;

import javafx.scene.web.WebEngine;

public class cssTweaks {

    // doesn't work, also with attempted forced repaint?
    public static void setStyles(WebEngine e){
        String js = """
                var styles = `
                  #minimap #vasquette #fineprint #scale{
                      visibility: hidden;
                  }
                  .app-viewcard-strip .widget-pane-toggle-button-container {
                      visibility: hidden;
                  }
                `                                
                var styleSheet = document.createElement("style")
                styleSheet.type = "text/css"
                styleSheet.innerText = styles
                document.head.appendChild(styleSheet)
                
                document.body.style.display = "none";
                document.body.style.display = "block";
                """;
        e.executeScript(js);
    }

    public static void styleClasses(WebEngine e, String... classes) {
        String script = """
                  function bla(){
                      var elements = document.getElementsByClassName("%s");
                      for (var i = 0, max = elements.length; i < max; i++) {
                         elements[i].style.display = "none";
                      }
                  } 
                setTimeout(bla, 8000);
                setTimeout(bla, 16000);
                """;
        for (String s : classes) {
            System.out.println(String.format(script, s));
            e.executeScript(String.format(script, s));
        }
    }

    public static void styleIds(WebEngine e, String... ids) {
        String script = """
                function bla(){
                    var e = document.getElementById("%s");
                    if(e)
                    {
                        e.style.display = "none";
                    }              
                } 
                setTimeout(bla, 8000);
                setTimeout(bla, 16000);
                """;
        for (String s : ids) {
            e.executeScript(String.format(script, s));
        }
    }

}
