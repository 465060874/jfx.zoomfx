package dejv.jfx.zoomfx;

import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import dejv.jfx.commons.geometry.ObservableBounds;
import dejv.jfx.commons.geometry.ObservableDimension2D;
import dejv.jfx.commons.geometry.ObservablePoint2D;
import dejv.jfx.zoomfx.internal.PanningController;

/**
 * JavaFX container, that allows to freely zoom and scroll its content.
 * <p>
 * @since 1.0.0
 * @author dejv78 (www.github.com/dejv78)
 */
@DefaultProperty("content")
public class ZoomFX
        extends GridPane {

    private static final double SCROLLING_DIVISOR = 200.0d;
    private static final double SCROLL_MIN = 0.0;
    private static final double SCROLL_MAX = 1.0;
    private static final double SCROLL_UNIT_INC = 0.1;

    // Properties
    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);

    //Sub-controls
    private final ScrollBar hscroll = new ScrollBar();
    private final ScrollBar vscroll = new ScrollBar();
    private final Pane contentPane = new Pane();
    private final Group contentGroup = new Group();
    private final Rectangle clip = new Rectangle();

    private final PanningController panningController;


    public ZoomFX() {
        setupScroll(hscroll, Orientation.HORIZONTAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);
        setupScroll(vscroll, Orientation.VERTICAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);

        panningController = new PanningController(contentPane, hscroll, vscroll, () -> setCursor(Cursor.MOVE), () -> setCursor(Cursor.DEFAULT));

        setupConstraints();
        setupStyle();
        setupClipping();
        setupBindings();

        contentPane.getChildren().add(contentGroup);
        getChildren().addAll(contentPane, hscroll, vscroll);

        hscroll.setValue(0.5);
        vscroll.setValue(0.5);
    }


    /**
     * @return The list of contained nodes.
     */
    public ObservableList<Node> getContent() {
        return contentGroup.getChildren();
    }


    /**
     * @return The container (Pane), that actually holds the content.
     */
    public Node getViewport() {
        return contentPane;
    }


    /**
     * @return The zoom factor of the content. Value of 1.0 means 1:1 content size, 0.5 means 1:2, etc.
     */
    public double getZoomFactor() {
        return zoomFactor.get();
    }


    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }


    public DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }


    /**
     * @return whether the panning is enabled, or not. If enabled, dragging with middle mouse button pans the view.
     */
    public boolean isPanEnabled() {
        return panningController.isPanEnabled();
    }


    public final void setPanEnabled(boolean panEnabled) {
        panningController.setPanEnabled(panEnabled);
    }


    private void setupScroll(final ScrollBar scroll, final Orientation orientation, final double min, final double max, final double unitIncrement) {
        scroll.setOrientation(orientation);
        scroll.setMin(min);
        scroll.setMax(max);
        scroll.setUnitIncrement(unitIncrement);
    }


    private void setupConstraints() {
        final ColumnConstraints c1 = new ColumnConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
        final ColumnConstraints c2 = new ColumnConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, false);
        final RowConstraints r1 = new RowConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true);
        final RowConstraints r2 = new RowConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, false);

        getColumnConstraints().addAll(c1, c2);
        getRowConstraints().addAll(r1, r2);

        GridPane.setConstraints(contentPane, 0, 0);
        GridPane.setConstraints(hscroll, 0, 1);
        GridPane.setConstraints(vscroll, 1, 0);
    }


    private void setupClipping() {
        clip.widthProperty().bind(contentPane.widthProperty());
        clip.heightProperty().bind(contentPane.heightProperty());
        contentPane.setClip(clip);
    }


    private void setupStyle() {
        contentPane.setStyle("-fx-background-color: GREY");
        this.setStyle("-fx-border-width: 0px");
    }


    private void setupBindings() {
        final ObservableDimension2D viewportPhysicalSize = new ObservableDimension2D();
        final ObservableBounds contentLogicalBounds = new ObservableBounds();

        contentPane.setOnScroll((event) -> {
            double mult = 1.0d + (event.getDeltaY() / SCROLLING_DIVISOR);
            zoomFactor.set(zoomFactor.get() * mult);
        });

        contentPane.layoutBoundsProperty().addListener((sender, oldValue, newValue) -> {
            viewportPhysicalSize.setWidth(newValue.getWidth());
            viewportPhysicalSize.setHeight(newValue.getHeight());
        });

        contentGroup.boundsInLocalProperty().addListener((sender, oldValue, newValue) -> {
            contentLogicalBounds.setMinX(newValue.getMinX());
            contentLogicalBounds.setMinY(newValue.getMinY());
            contentLogicalBounds.setMaxX(newValue.getMaxX());
            contentLogicalBounds.setMaxY(newValue.getMaxY());
        });

        final Scale scale = new Scale();
        final Translate translate = new Translate();

        final ObservableDimension2D viewportLogicalHalfSize = new ObservableDimension2D(
                viewportPhysicalSize.widthProperty().divide(zoomFactor).multiply(0.5),
                viewportPhysicalSize.heightProperty().divide(zoomFactor).multiply(0.5));

        final ObservableBounds pivotLogicalExtent = new ObservableBounds(
                contentLogicalBounds.minXProperty().add(viewportLogicalHalfSize.widthProperty()),
                contentLogicalBounds.minYProperty().add(viewportLogicalHalfSize.heightProperty()),
                contentLogicalBounds.maxXProperty().subtract(viewportLogicalHalfSize.widthProperty()),
                contentLogicalBounds.maxYProperty().subtract(viewportLogicalHalfSize.heightProperty()));

        panningController.initBindings(pivotLogicalExtent.widthProperty(), pivotLogicalExtent.heightProperty(), zoomFactor);

        final ObservablePoint2D pivotLogicalCoords = new ObservablePoint2D(
                pivotLogicalExtent.minXProperty().add(pivotLogicalExtent.widthProperty().multiply(hscroll.valueProperty())),
                pivotLogicalExtent.minYProperty().add(pivotLogicalExtent.heightProperty().multiply(vscroll.valueProperty())));

        final ObservableBounds viewportLogicalBounds = new ObservableBounds(
                pivotLogicalCoords.xProperty().subtract(viewportLogicalHalfSize.widthProperty()),
                pivotLogicalCoords.yProperty().subtract(viewportLogicalHalfSize.heightProperty()),
                pivotLogicalCoords.xProperty().add(viewportLogicalHalfSize.widthProperty()),
                pivotLogicalCoords.yProperty().add(viewportLogicalHalfSize.heightProperty()));

        hscroll.visibleAmountProperty().bind(viewportLogicalBounds.widthProperty().divide(contentLogicalBounds.widthProperty()));
        vscroll.visibleAmountProperty().bind(viewportLogicalBounds.heightProperty().divide(contentLogicalBounds.heightProperty()));

        translate.xProperty().bind(viewportLogicalBounds.minXProperty().multiply(-1));
        translate.yProperty().bind(viewportLogicalBounds.minYProperty().multiply(-1));
        scale.xProperty().bind(zoomFactor);
        scale.yProperty().bind(zoomFactor);

        contentGroup.getTransforms().addAll(scale, translate);
    }
}
