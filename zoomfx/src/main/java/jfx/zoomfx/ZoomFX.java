package jfx.zoomfx;

import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.dejv.jfx.common.binding.BiDiDoubleProperty;
import app.dejv.jfx.common.geometry.utils.RectangleUtils;
import app.dejv.jfx.zoomfx.logic.ZoomFXLogic;

/**
 * Zoomable ScrollPane for JavaFX.
 * <br/>
 *
 * @author dejv78 (www.github.com/dejv78)
 */
@DefaultProperty("content")
public class ZoomFX
        extends GridPane {

    private static final double SCROLLING_DIVISOR = 200.0d;
    private static final double SCROLL_MIN = 0.0;
    private static final double SCROLL_MAX = 1.0;
    private static final double SCROLL_UNIT_INC = 0.0000001;
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(ZoomFX.class);
    // Subcontrols
    private final ScrollBar hscroll = new ScrollBar();
    private final ScrollBar vscroll = new ScrollBar();
    private final Pane contentPane = new Pane();
    private final Group contentGroup = new Group();
    private final Rectangle clip = new Rectangle();
    private final ColumnConstraints c1 = new ColumnConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
    private final ColumnConstraints c2 = new ColumnConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, false);
    private final RowConstraints r1 = new RowConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true);
    private final RowConstraints r2 = new RowConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, false);
    // Transforms
    private final Scale scaleTransform = new Scale();
    private final Translate translateTransform = new Translate();

    // Properties
    private final DoubleProperty zoomFactor;
    private final EventHandler<MouseEvent> onPanMouseRelease = (event) -> exitPan();
    private final EventHandler<KeyEvent> onPanKeyRelease = (event) -> exitPan();

    // Logic
    private ZoomFXLogic controller = new ZoomFXLogic(this);
    private Point2D dragStart = null;
    private boolean panIsEnabled = false;


    /**
     * Create new ZoomableScrollPane
     */
    public ZoomFX() {
        super();

        zoomFactor = new BiDiDoubleProperty(this, "zoomFactor");

        contentGroup.getTransforms().add(scaleTransform);
        contentGroup.getTransforms().add(translateTransform);

        setupScroll(hscroll, Orientation.HORIZONTAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);
        setupScroll(vscroll, Orientation.VERTICAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);

        setupClipping();
        setupConstraints();
        setupStyle();

        contentPane.getChildren().add(contentGroup);
        getChildren().addAll(contentPane, hscroll, vscroll);

        this.zoomFactor.set(1.0);
        hscroll.setValue(0.5);
        vscroll.setValue(0.5);

        installPan();

        contentPane.layoutBoundsProperty().addListener(
                (observable, oldValue, newValue) -> runWithController(
                        () -> controller.viewportPhysicalBoundsUpdated(RectangleUtils.fromBounds(newValue))));

        contentPane.setOnScroll((event) -> {
            double mult = 1.0d + (event.getDeltaY() / SCROLLING_DIVISOR);
            zoomFactor.setValue(zoomFactor.get() * mult);
        });


        contentGroup.boundsInLocalProperty().addListener(
                (observable, oldValue, newValue) -> runWithController(
                        () -> controller.contentLogicalBoundsUpdated(RectangleUtils.fromBounds(newValue))));

        hscroll.valueProperty().addListener(
                (observable, oldValue, newValue) -> runWithController(
                        () -> controller.horizontalScrollValueUpdated(newValue.doubleValue())));

        vscroll.valueProperty().addListener(
                (observable, oldValue, newValue) -> runWithController(
                        () -> controller.verticalScrollValueUpdated(newValue.doubleValue())));

        zoomFactor.addListener(
                (observable, oldValue, newValue) -> runWithController(
                        () -> controller.zoomFactorUpdated(newValue.doubleValue())));

    }


    /**
     * Create new ZoomableScrollPane, and populate it with given children
     *
     * @param children List of children nodes
     */
    public ZoomFX(final Node... children) {
        this();
        contentGroup.getChildren().addAll(children);
    }


    /**
     * @return List of children nodes
     */
    public ObservableList<Node> getContent() {
        return contentGroup.getChildren();
    }


    /**
     * @return Zoom factor of the view
     */
    public final double getZoomFactor() {
        return zoomFactor.get();
    }


    /**
     * Set the zoom factor of the view
     *
     * @param value Zoom factor (1.0 = no zoom)
     */
    public final void setZoomFactor(final double value) {
        zoomFactorProperty().set(value);
    }


    /**
     * @return Zoom factor observable property
     */
    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }


    public void setHScrollVisibleAmount(final double amount) {
        hscroll.setVisibleAmount(amount);
    }


    public void setHScrollValue(final double value) {
        LOG.trace("HSCROLL (value: {})", value);
        hscroll.setValue(value);
    }


    public void setVScrollVisibleAmount(final double amount) {
        vscroll.setVisibleAmount(amount);
    }


    public void setVScrollValue(final double value) {
        LOG.trace("VSCROLL (value: {})", value);
        vscroll.setValue(value);
    }


    public void setContentScaleFactor(final double scaleFactor) {
        scaleTransform.setX(scaleFactor);
        scaleTransform.setY(scaleFactor);
    }


    public void setContentScalePivot(final double px, final double py) {
        LOG.trace("CONTENT SCALE PIVOT (px: {}, py: {})", px, py);
        scaleTransform.setPivotX(px);
        scaleTransform.setPivotY(py);
    }


    public void setContentTranslation(final double tx, final double ty) {
        LOG.trace("CONTENT TRANSLATION (tx: {}, ty: {})", tx, ty);
        translateTransform.setX(tx);
        translateTransform.setY(ty);
    }


    public void setPanningEnabled(boolean enabled) {
        if (enabled) {
            installPan();
        } else {
            uninstallPan();
        }
    }


    public Node getNode() {
        return contentPane;
    }


    public Group getContentGroup() {
        return contentGroup;
    }

    private void setupScroll(final ScrollBar scroll, final Orientation orientation, final double min, final double max, final double unitIncrement) {
        scroll.setOrientation(orientation);
        scroll.setMin(min);
        scroll.setMax(max);
        scroll.setUnitIncrement(unitIncrement);
    }


    private void setupConstraints() {
        getColumnConstraints().addAll(c1, c2);
        getRowConstraints().addAll(r1, r2);

        GridPane.setConstraints(contentPane, 0, 0);
        GridPane.setConstraints(hscroll, 0, 1);
        GridPane.setConstraints(vscroll, 1, 0);
    }


    private void setupStyle() {
        contentPane.setStyle("-fx-background-color: GREY");
        this.setStyle("-fx-border-width: 1px; -fx-border-color: GREY;");
    }


    private void setupClipping() {
        clip.widthProperty().bind(contentPane.widthProperty());
        clip.heightProperty().bind(contentPane.heightProperty());
        contentPane.setClip(clip);
    }


    private void runWithController(Runnable r) {
        if (controller != null) {
            r.run();
        } else {
            LOG.warn("Controller is not set");
        }
    }


    private void installPan() {
        if (panIsEnabled) {
            return;
        }

        contentPane.setOnMouseDragged((event) -> {
            if (event.isMiddleButtonDown()) {

                if (dragStart == null) {
                    enterPan(event.getSceneX(), event.getSceneY());
                    return;
                }

                runWithController(() -> {
                    controller.pan(event.getSceneX() - dragStart.getX(), event.getSceneY() - dragStart.getY());
                    dragStart = new Point2D(event.getSceneX(), event.getSceneY());
                });
            }
        });

        contentPane.setOnMouseDragExited((event) -> {
            LOG.debug(" EDRAG x:{} y:{}", event.getSceneX(), event.getSceneY());
            dragStart = null;
            setCursor(Cursor.DEFAULT);
        });
        panIsEnabled = true;
    }


    private void uninstallPan() {
        if (!panIsEnabled) {
            return;
        }
        if (dragStart != null) {
            exitPan();
        }
        contentPane.setOnMouseDragged(null);
        contentPane.setOnMouseDragExited(null);

        panIsEnabled = false;
    }


    private void enterPan(double x, double y) {
        dragStart = new Point2D(x, y);
        setCursor(Cursor.MOVE);

        contentPane.addEventHandler(MouseEvent.MOUSE_RELEASED, onPanMouseRelease);
        contentPane.addEventHandler(KeyEvent.KEY_RELEASED, onPanKeyRelease);

    }


    private void exitPan() {
        setCursor(Cursor.DEFAULT);
        dragStart = null;
        contentPane.removeEventHandler(MouseEvent.MOUSE_RELEASED, onPanMouseRelease);
        contentPane.removeEventHandler(KeyEvent.KEY_RELEASED, onPanKeyRelease);
    }
}
