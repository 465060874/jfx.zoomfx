package jfx.zoomfx.internal;

import static java.util.Objects.requireNonNull;

import javafx.beans.binding.DoubleExpression;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * <br/>
 *
 * @author dejv78 (www.github.com/dejv78)
 */
public class PanningController {

    private final Pane pane;
    private final ScrollBar hscroll;
    private final ScrollBar vscroll;

    private final EventHandler<MouseEvent> onPanMouseRelease = (event) -> exitPan();
    private final EventHandler<KeyEvent> onPanKeyRelease = (event) -> exitPan();
    private final Runnable onEnterPan;
    private final Runnable onExitPan;

    private DoubleExpression hExtent;
    private DoubleExpression vExtent;
    private DoubleExpression zoomFactor;

    private boolean initialized = false;
    private boolean panEnabled = false;
    private Point2D dragStart = null;


    public PanningController(Pane pane, ScrollBar hscroll, ScrollBar vscroll, Runnable onEnterPan, Runnable onExitPan) {
        requireNonNull(pane, "Parameter 'pane' is null");
        requireNonNull(hscroll, "Parameter 'hscroll' is null");
        requireNonNull(vscroll, "Parameter 'vscroll' is null");
        requireNonNull(onEnterPan, "Parameter 'onEnterPan' is null");
        requireNonNull(onExitPan, "Parameter 'onExitPan' is null");

        this.pane = pane;
        this.hscroll = hscroll;
        this.vscroll = vscroll;

        this.onEnterPan = onEnterPan;
        this.onExitPan = onExitPan;

    }


    public void initBindings(DoubleExpression hExtent, DoubleExpression vExtent, DoubleExpression zoomFactor) {
        requireNonNull(hExtent, "Parameter 'hExtent' is null");
        requireNonNull(vExtent, "Parameter 'vExtent' is null");
        requireNonNull(zoomFactor, "Parameter 'zoomFactor' is null");

        this.hExtent = hExtent;
        this.vExtent = vExtent;
        this.zoomFactor = zoomFactor;

        initialized = true;
        installPan();
    }


    public boolean isPanEnabled() {
        return panEnabled;
    }


    public final void setPanEnabled(boolean panEnabled) {
        if (panEnabled) {
            installPan();
        } else {
            uninstallPan();
        }
    }


    private void installPan() {
        if ((!initialized) || (panEnabled)) {
            return;
        }

        pane.setOnMouseDragged((event) -> {
            if (event.isMiddleButtonDown()) {
                if (dragStart == null) {
                    enterPan(event.getSceneX(), event.getSceneY());
                    return;
                }

                final double dX = (event.getSceneX() - dragStart.getX()) / (hExtent.get() * zoomFactor.get());
                final double dY = (event.getSceneY() - dragStart.getY()) / (vExtent.get() * zoomFactor.get());

                hscroll.setValue(hscroll.getValue() - dX);
                vscroll.setValue(vscroll.getValue() - dY);
                dragStart = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        pane.setOnMouseDragExited((event) -> exitPan());
        panEnabled = true;
    }


    private void uninstallPan() {
        if (!panEnabled) {
            return;
        }
        if (dragStart != null) {
            exitPan();
        }
        pane.setOnMouseDragged(null);
        pane.setOnMouseDragExited(null);

        panEnabled = false;
    }


    private void enterPan(double x, double y) {
        onEnterPan.run();

        dragStart = new Point2D(x, y);

        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, onPanMouseRelease);
        pane.addEventHandler(KeyEvent.KEY_RELEASED, onPanKeyRelease);
    }


    private void exitPan() {
        dragStart = null;
        pane.removeEventHandler(MouseEvent.MOUSE_RELEASED, onPanMouseRelease);
        pane.removeEventHandler(KeyEvent.KEY_RELEASED, onPanKeyRelease);

        onExitPan.run();
    }

}
