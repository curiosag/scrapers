package google.maps.tiles;

import javafx.scene.paint.Color;

import java.util.Arrays;

public enum COLORS_WANTED {
    religious(120, 144, 156),
    finance(121, 134, 203),
    cultural(18, 181, 203),
    hospital(238, 103, 92),
    shop(84, 149, 237),
    restaurant(242, 153, 0);

    public final Color color;

    COLORS_WANTED(int r, int g, int b) {
        this.color = new Color(r / 255d, g/ 255d, b/ 255d, 0);
    }

    public boolean isWanted(Color c) {
        return Arrays.stream(COLORS_WANTED.values()).anyMatch(i -> i.color.equals(c));
    }
}