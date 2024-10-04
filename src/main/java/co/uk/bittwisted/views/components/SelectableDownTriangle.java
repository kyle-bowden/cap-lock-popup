package co.uk.bittwisted.views.components;

import java.awt.*;

public class SelectableDownTriangle extends UIComponent {
    public SelectableDownTriangle(int x, int y) {
        super();
        this.shape = new Polygon(
                new int[]{x, x+25, x+12},
                new int[]{y, y, y+25},
                3
        );
    }
}
