package co.uk.bittwisted.views.components;

import java.awt.geom.Ellipse2D;

public class SelectableCircle extends UIComponent {
    public SelectableCircle(int x, int y, int width, int height) {
        super();
        this.shape = new Ellipse2D.Double(x, y, width, height);
    }
}
