package google.maps.webview.markers;

public class RGB {

    public static int red(int rgb){
        return (rgb >> 16) & 0x000000FF;
    }

    public static int green(int rgb){
        return (rgb >> 8) & 0x000000FF;
    }

    public static int blue(int rgb){
        return rgb & 0x000000FF;
    }

    public static int alpha(int rgb){
        return (rgb >> 24) & 0x000000FF;
    }
}
