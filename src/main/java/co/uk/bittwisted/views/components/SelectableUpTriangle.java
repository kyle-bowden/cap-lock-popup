package co.uk.bittwisted.views.components;

import java.awt.*;

public class SelectableUpTriangle extends UIComponent {
    public SelectableUpTriangle(int x, int y) {
        super();
        this.shape = new Polygon(
                new int[]{x, x+25, x+12},
                new int[]{y+25, y+25, y},
                3
        );
    }
}
