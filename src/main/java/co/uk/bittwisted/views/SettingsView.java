package co.uk.bittwisted.views;

import co.uk.bittwisted.CapsLockHook;
import co.uk.bittwisted.config.AppConfig;
import co.uk.bittwisted.enums.Position;
import co.uk.bittwisted.util.Helpers;
import co.uk.bittwisted.views.components.SelectableDownTriangle;
import co.uk.bittwisted.views.components.SelectableRoundRect;
import co.uk.bittwisted.views.components.SelectableUpTriangle;
import co.uk.bittwisted.views.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;

public class SettingsView extends JFrame {
    private final int WINDOW_WIDTH = 250;
    private final int WINDOW_HEIGHT = 250;
    private final SelectableRoundRect[] selectableRects = new SelectableRoundRect[6];

    private final AppConfig appConfig;

    public final Font defaultFont = new Font("Arial Black", Font.PLAIN, 25);
    public final Font propertyFont = new Font("Arial Black", Font.PLAIN, 50);
    private final CapsLockHook capsLockHook;

    private final Image offScreen;
    private final Graphics2D buffer;

    private final SelectableUpTriangle upArrow = new SelectableUpTriangle(205, 100);
    private final SelectableDownTriangle downArrow = new SelectableDownTriangle(205, 140);

    private final GradientPaint defaultBackgroundGradient = new GradientPaint(100, 0, Color.BLACK, WINDOW_WIDTH, WINDOW_HEIGHT, Color.GRAY);

    public SettingsView(CapsLockHook clh, AppConfig config) {
        this.capsLockHook = clh;
        this.appConfig = config;

        setTitle("Settings");
        setType(Type.UTILITY);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(250, 250);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);

        Position position = Position.valueOf(config.getLocation());
        //35
        int TOP_PADDING = 38;
        int RECT_WIDTH = 40;
        //12
        int OFFSET = 15;
        selectableRects[0] = new SelectableRoundRect(OFFSET, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_LEFT, Position.TOP_LEFT);
        selectableRects[1] = new SelectableRoundRect(WINDOW_WIDTH - OFFSET - RECT_WIDTH, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_RIGHT, Position.TOP_RIGHT);
        selectableRects[2] = new SelectableRoundRect(OFFSET, WINDOW_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_LEFT, Position.BOTTOM_LEFT);
        selectableRects[3] = new SelectableRoundRect(WINDOW_WIDTH - OFFSET - RECT_WIDTH, WINDOW_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_RIGHT, Position.BOTTOM_RIGHT);
        selectableRects[4] = new SelectableRoundRect(WINDOW_WIDTH / 2 - RECT_WIDTH / 2, TOP_PADDING, RECT_WIDTH, RECT_WIDTH, position == Position.TOP_CENTER, Position.TOP_CENTER);
        selectableRects[5] = new SelectableRoundRect(WINDOW_WIDTH / 2 - RECT_WIDTH / 2, WINDOW_WIDTH - OFFSET - RECT_WIDTH, RECT_WIDTH, RECT_WIDTH, position == Position.BOTTOM_CENTER, Position.BOTTOM_CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Arrays.stream(selectableRects).forEach(selectableRect -> {
                    if(selectableRect.selected &&
                        selectableRect.position == Position.valueOf(config.getLocation()))  {
                        return;
                    }

                    if(selectableRect.shape.contains(new Point(e.getX(), e.getY()))) {
                        Arrays.stream(selectableRects).forEach(selectableRoundRect -> selectableRoundRect.selected = false);
                        selectableRect.selected = true;
                        capsLockHook.updatePopupPosition(selectableRect.position);
                    }
                    repaint();
                });

                upArrow.selected = upArrow.shape.contains(new Point(e.getX(), e.getY()));
                if(upArrow.selected) {
                    System.out.println("Up");
                    capsLockHook.updatePopupDelay(true);
                }
                downArrow.selected = downArrow.shape.contains(new Point(e.getX(), e.getY()));
                if(downArrow.selected) {
                    System.out.println("Down");
                    capsLockHook.updatePopupDelay(false);
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                Arrays.stream(selectableRects).forEach(selectableRect -> {
                    selectableRect.focused = selectableRect.shape.contains(new Point(e.getX(), e.getY()));
                    repaint();
                });

                upArrow.focused = upArrow.shape.contains(new Point(e.getX(), e.getY()));
                downArrow.focused = downArrow.shape.contains(new Point(e.getX(), e.getY()));
            }
        });

        setAlwaysOnTop(true);

        setVisible(true);
        offScreen = createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
        buffer = (Graphics2D) offScreen.getGraphics();
        setVisible(false);
    }

    @Override
    public void paint(Graphics g) {
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
            buffer.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

            Arrays.stream(selectableRects).forEach(selectableRect -> {
                if (selectableRect.selected) {
                    buffer.setColor(Color.WHITE);
                    buffer.fill(selectableRect.shape);

                    buffer.setColor(Color.BLACK);
                    buffer.setFont(defaultFont);
                    buffer.drawString("A", selectableRect.shape.getBounds().x + 10, selectableRect.shape.getBounds().y + 28);
                } else {
                    if (selectableRect.focused) {
                        buffer.setColor(Color.GRAY);
                        buffer.fill(selectableRect.shape);
                    }

                    buffer.setColor(Color.WHITE);
                    buffer.draw(selectableRect.shape);
                }
            });

            paintWhenComponentFocused(upArrow);
            paintWhenComponentFocused(downArrow);

            buffer.setColor(Color.GRAY);
            buffer.setFont(propertyFont);
            float popupDelay = Float.parseFloat(appConfig.getPopUpDelay());
            buffer.drawString(Helpers.formatWithOneDecimalPlace(popupDelay) +"s", 60, 150);

            g.drawImage(offScreen, 0, 0, this);
        }
    }

    private void paintWhenComponentFocused(UIComponent component) {
        if(component.focused) {
            buffer.setColor(Color.GRAY);
            buffer.fill(component.shape);
        }
        buffer.setColor(Color.WHITE);
        buffer.draw(component.shape);
    }

    public void showSettings() {
        setVisible(true);
    }
}
