package co.uk.bittwisted.views.components;

import co.uk.bittwisted.enums.Position;

import java.awt.geom.RoundRectangle2D;

public class SelectableRoundRect extends UIComponent {
    public SelectableRoundRect(int x, int y, int w, int h, boolean s, Position p) {
        super(s, p);
        this.shape = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
    }
}
