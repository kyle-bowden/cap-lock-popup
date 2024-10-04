package co.uk.bittwisted.views.components;

import co.uk.bittwisted.enums.Position;

import java.awt.*;

public class UIComponent {
    public boolean focused;
    public boolean selected;

    public final Position position;
    public Shape shape;

    public UIComponent() {
        this.selected = false;
        position = Position.NONE;
    }

    public UIComponent(boolean s, Position p) {
        this.selected = s;
        this.position = p;
    }
}
