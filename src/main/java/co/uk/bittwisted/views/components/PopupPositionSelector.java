package co.uk.bittwisted.views.components;

import co.uk.bittwisted.CapsLockHook;
import co.uk.bittwisted.config.AppConfig;
import co.uk.bittwisted.enums.PopupSize;
import co.uk.bittwisted.enums.Position;
import co.uk.bittwisted.util.Helpers;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.logging.Logger;

public class PopupPositionSelector extends JComponent {
    private final AppConfig appConfig;
    private final Logger logger = Logger.getLogger(PopupPositionSelector.class.getName());

    private final int COMPONENT_WIDTH = 250;
    private final int COMPONENT_HEIGHT = 250;
    private final SelectableRoundRect[] selectablePositionRects = new SelectableRoundRect[6];

    private Graphics2D buffer;

    private final Color selectedColor = new Color(187, 187, 187);
    private final Color focusColor = new Color(62, 101, 145);
    private final SelectableUpTriangle upArrow = new SelectableUpTriangle(205, 110);
    private final SelectableDownTriangle downArrow = new SelectableDownTriangle(205, 150);

    private final SelectableCircle smallCircle = new SelectableCircle(70, 81, 20, 20);
    private final SelectableCircle mediumCircle = new SelectableCircle(112, 78, 25, 25);
    private final SelectableCircle largeCircle = new SelectableCircle(157, 75, 30, 30);

    public final Font defaultFont       = new Font("Arial Black", Font.PLAIN, 25);
    public final Font popupDelayFont    = new Font("Arial Black", Font.PLAIN, 50);
    private final GradientPaint defaultBackgroundGradient =
            new GradientPaint(0,
                    COMPONENT_HEIGHT / 2,
                    new Color(60, 63, 65),
                    COMPONENT_WIDTH,
                    COMPONENT_HEIGHT / 2,
                    new Color(78, 80, 82));

    public PopupPositionSelector(CapsLockHook clh, AppConfig appConfig) {
        this.appConfig = appConfig;

        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        Position position = Position.valueOf(appConfig.getPosition());

        //35
        int TOP_PADDING = 20;
        int RECT_WIDTH = 40;
        //12
        int OFFSET = 15;
        selectablePositionRects[0] = new SelectableRoundRect(OFFSET, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_LEFT, Position.TOP_LEFT);
        selectablePositionRects[1] = new SelectableRoundRect(COMPONENT_WIDTH - OFFSET - RECT_WIDTH, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_RIGHT, Position.TOP_RIGHT);
        selectablePositionRects[2] = new SelectableRoundRect(OFFSET, COMPONENT_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_LEFT, Position.BOTTOM_LEFT);
        selectablePositionRects[3] = new SelectableRoundRect(COMPONENT_WIDTH - OFFSET - RECT_WIDTH, COMPONENT_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_RIGHT, Position.BOTTOM_RIGHT);
        selectablePositionRects[4] = new SelectableRoundRect(COMPONENT_WIDTH / 2 - RECT_WIDTH / 2, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_CENTER, Position.TOP_CENTER);
        selectablePositionRects[5] = new SelectableRoundRect(COMPONENT_WIDTH / 2 - RECT_WIDTH / 2, COMPONENT_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_CENTER, Position.BOTTOM_CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Arrays.stream(selectablePositionRects).forEach(selectableRect -> {
                    if(selectableRect.selected &&
                        selectableRect.position == Position.valueOf(appConfig.getPosition()))  {
                        return;
                    }

                    if(selectableRect.shape.contains(new Point(e.getX(), e.getY()))) {
                        Arrays.stream(selectablePositionRects).forEach(selectableRoundRect -> selectableRoundRect.selected = false);
                        selectableRect.selected = true;
                        clh.setPopupPosition(selectableRect.position);
                    }

                    repaint();
                });

                upArrow.selected = upArrow.shape.contains(new Point(e.getX(), e.getY()));
                if(upArrow.selected) {
                    clh.setPopupDelay(true);
                }
                downArrow.selected = downArrow.shape.contains(new Point(e.getX(), e.getY()));
                if(downArrow.selected) {
                    clh.setPopupDelay(false);
                }

                smallCircle.selected = smallCircle.shape.contains(new Point(e.getX(), e.getY()));
                if(smallCircle.selected) {
                    clh.setPopUpSize(PopupSize.SMALL);
                    clh.setPopupPosition(Position.valueOf(appConfig.getPosition()));
                }

                mediumCircle.selected = mediumCircle.shape.contains(new Point(e.getX(), e.getY()));
                if(mediumCircle.selected) {
                    clh.setPopUpSize(PopupSize.MEDIUM);
                    clh.setPopupPosition(Position.valueOf(appConfig.getPosition()));
                }

                largeCircle.selected = largeCircle.shape.contains(new Point(e.getX(), e.getY()));
                if(largeCircle.selected) {
                    clh.setPopUpSize(PopupSize.LARGE);
                    clh.setPopupPosition(Position.valueOf(appConfig.getPosition()));
                }

                setSelectedSize();
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                Arrays.stream(selectablePositionRects).forEach(selectableRect -> {
                    selectableRect.focused = selectableRect.shape.contains(new Point(e.getX(), e.getY()));
                    repaint();
                });

                upArrow.focused     = upArrow.shape.contains(new Point(e.getX(), e.getY()));
                downArrow.focused   = downArrow.shape.contains(new Point(e.getX(), e.getY()));

                smallCircle.focused   = smallCircle.shape.contains(new Point(e.getX(), e.getY()));
                mediumCircle.focused  = mediumCircle.shape.contains(new Point(e.getX(), e.getY()));
                largeCircle.focused   = largeCircle.shape.contains(new Point(e.getX(), e.getY()));
            }
        });

        setSelectedSize();
    }

    private void setSelectedSize() {
        smallCircle.selected  = false;
        mediumCircle.selected = false;
        largeCircle.selected  = false;

        switch (PopupSize.valueOf(appConfig.getPopUpSize())) {
            case SMALL    -> smallCircle.selected  = true;
            case MEDIUM   -> mediumCircle.selected = true;
            case LARGE    -> largeCircle.selected  = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        buffer = (Graphics2D) g;
        if (buffer != null) {
            buffer.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            buffer.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            buffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            buffer.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            buffer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            buffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            buffer.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            buffer.setPaint(defaultBackgroundGradient);
            buffer.fillRect(0, 0, COMPONENT_WIDTH, COMPONENT_HEIGHT);

            buffer.setColor(Color.WHITE);
            Arrays.stream(selectablePositionRects).forEach(selectableRect -> {
                if (selectableRect.selected) {
                    paintWhenComponentSelected(selectableRect);
                    buffer.setColor(Color.BLACK);
                    buffer.setFont(defaultFont);
                    buffer.drawString("A", selectableRect.shape.getBounds().x + 10, selectableRect.shape.getBounds().y + 28);
                } else {
                    paintWhenComponentFocused(selectableRect);
                }
            });

            paintWhenComponentFocused(smallCircle);
            paintWhenComponentFocused(mediumCircle);
            paintWhenComponentFocused(largeCircle);
            paintWhenComponentSelected(smallCircle);
            paintWhenComponentSelected(mediumCircle);
            paintWhenComponentSelected(largeCircle);

            paintWhenComponentFocused(upArrow);
            paintWhenComponentFocused(downArrow);

            buffer.setColor(selectedColor);
            buffer.setFont(popupDelayFont);
            float popupDelay = Float.parseFloat(appConfig.getPopUpDelay());
            buffer.drawString(Helpers.formatWithOneDecimalPlace(popupDelay) +"s", 60, 160);
        }
    }

    private void paintWhenComponentFocused(UIComponent component) {
        if(component.focused) {
            buffer.setColor(focusColor);
            buffer.fill(component.shape);
        }
        buffer.setColor(Color.WHITE);
        buffer.draw(component.shape);
    }

    private void paintWhenComponentSelected(UIComponent component) {
        if(component.selected) {
            buffer.setColor(selectedColor);
            buffer.fill(component.shape);
        }
        buffer.setColor(Color.WHITE);
        buffer.draw(component.shape);
    }
}
