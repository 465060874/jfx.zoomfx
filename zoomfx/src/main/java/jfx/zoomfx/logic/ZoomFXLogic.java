package jfx.zoomfx.logic;

import static java.util.Objects.requireNonNull;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.dejv.jfx.common.geometry.utils.RectangleUtils;
import jfx.zoomfx.ZoomFX;

/**
 * Logic for the ZoomFX pane.
 * <br/>
 *
 * @author dejv78 (www.dejv.info)
 */
public class ZoomFXLogic {

    private Rectangle2D dummyBounds = new Rectangle2D(0,0,0,0);
    private Point2D dummyPivot = new Point2D(0,0);

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(ZoomFXLogic.class);
    private ZoomFX view;
    private Rectangle2D contentLogicalBounds = dummyBounds;
    private Rectangle2D viewportPhysicalBounds = dummyBounds;
    private Rectangle2D viewportLogicalBounds = dummyBounds;
    private Rectangle2D extentLogicalBounds = dummyBounds;
    private Rectangle2D extentPannableArea = dummyBounds;
    private Point2D pivotLogicalCoords = dummyPivot;
    private double hscrollValue = 0.5;
    private double vscrollValue = 0.5;
    private double scale = 1.0;
    private boolean updatingScroll = false;
    private boolean pivotFixed = false;
    private boolean viewportInitialized = false;


    /**
     * Create new zoomable scroll-pane Controller for a given View
     *
     * @param view The target view
     */
    public ZoomFXLogic(final ZoomFX view) {
        requireNonNull(view, "view is null");
        this.view = view;
    }


    /**
     * Create new zoomable scroll-pane Controller for a given View
     *
     * @param view         View
     * @param initialPivot Initial pivot coords
     */
    public ZoomFXLogic(final ZoomFX view, final Point2D initialPivot) {
        this(view);

        requireNonNull(initialPivot, "initialPivot is null");

        pivotLogicalCoords = initialPivot;
        pivotFixed = true;
    }


    public void viewportPhysicalBoundsUpdated(final Rectangle2D viewportPhysicalBounds) {
        requireNonNull(viewportPhysicalBounds, "viewportPhysicalBounds is NULL");

        LOG.trace("---> viewportPhysicalBoundsUpdated(viewportPhysicalBounds:{})", viewportPhysicalBounds);

        this.viewportPhysicalBounds = viewportPhysicalBounds;

        if ((viewportPhysicalBounds.getWidth() > 0) && (viewportPhysicalBounds.getHeight() > 0)) {
            viewportInitialized = true;
        }

        refreshLogicalViewport();
    }


    public void contentLogicalBoundsUpdated(final Rectangle2D contentLogicalBounds) {
        requireNonNull(contentLogicalBounds, "contentLogicalBounds is NULL");

        LOG.trace("---> contentLogicalBoundsUpdated(contentLogicalBounds:{})", contentLogicalBounds);

        this.contentLogicalBounds = contentLogicalBounds;

        if (!pivotFixed) {
            refreshLogicalPivotUnfixed();
        }

        refreshLogicalExtent();
    }


    public void horizontalScrollValueUpdated(double hscroll) {

        hscroll = checkRange0_1(hscroll);

        if (!updatingScroll) {
            LOG.trace("---> horizontalScrollValueUpdated(hscroll:{})", hscroll);

            this.hscrollValue = hscroll;

            refreshLogicalPivotFixed();
        }
    }


    public void verticalScrollValueUpdated(double vscroll) {
        vscroll = checkRange0_1(vscroll);
        if (!updatingScroll) {
            LOG.trace("---> verticalScrollValueUpdated(vscroll:{})", vscroll);

            this.vscrollValue = vscroll;

            refreshLogicalPivotFixed();
        }
    }


    public void zoomFactorUpdated(double zoomFactor) {
        zoomFactor = checkRange0_inf(zoomFactor);

        LOG.trace("---> zoomFactorUpdated(zoomFactor:{})", zoomFactor);

        this.scale = zoomFactor;
        refreshLogicalViewport();
    }


    public void pan(double dx, double dy) {
        LOG.trace("---> pan(dx:{} dy:{})", dx, dy);

        refreshLogicalPivotPanned(dx, dy);
    }


    private void refreshLogicalPivotUnfixed() {
        pivotLogicalCoords = RectangleUtils.center(contentLogicalBounds);

        onLogicalPivotUpdated();
    }


    private void refreshLogicalPivotFixed() {
        if (viewportInitialized) {

            if (!pivotFixed) {
                pivotFixed = true;
            }

            final double pivotX = extentPannableArea.getMinX() + extentPannableArea.getWidth() * hscrollValue;
            final double pivotY = extentPannableArea.getMinY() + extentPannableArea.getHeight() * vscrollValue;
            pivotLogicalCoords = new Point2D(pivotX, pivotY);

            onLogicalPivotUpdated();
        }
    }


    private void refreshLogicalPivotPanned(final double dx, final double dy) {
        if (viewportInitialized) {

            if (!pivotFixed) {
                pivotFixed = true;
            }

            pivotLogicalCoords = new Point2D(pivotLogicalCoords.getX() - dx, pivotLogicalCoords.getY() - dy);

            onLogicalPivotUpdated();
        }
    }


    private void refreshLogicalViewport() {
        // Logical viewport = Pivot -/+ (Scaled physical viewport size)
        final double scaledWidth = viewportPhysicalBounds.getWidth() / scale;
        final double scaledHeight = viewportPhysicalBounds.getHeight() / scale;
        final double x = pivotLogicalCoords.getX() - scaledWidth / 2.0d;
        final double y = pivotLogicalCoords.getY() - scaledHeight / 2.0d;

        viewportLogicalBounds = new Rectangle2D(x, y, scaledWidth, scaledHeight);

        onLogicalViewportUpdated();
    }


    private void refreshLogicalExtent() {
        // Logical extent = union (Logical content, Logical viewport)
        extentLogicalBounds = RectangleUtils.union(contentLogicalBounds, viewportLogicalBounds);

        // Pannable area = Logical extent bounds - Logical viewport size
        extentPannableArea = RectangleUtils.centerResize(extentLogicalBounds, -viewportLogicalBounds.getWidth(), -viewportLogicalBounds.getHeight());

        final double eminX = extentPannableArea.getMinX();
        final double eminY = extentPannableArea.getMinY();
        final double emaxX = extentPannableArea.getMaxX();
        final double emaxY = extentPannableArea.getMaxY();

        // Recalculate scroll positions with regard to new viewport / extent relations
        if (!Precision.equals(emaxX - eminX, 0, Precision.EPSILON)) {
            hscrollValue = (pivotLogicalCoords.getX() - eminX) / (emaxX - eminX);
        }

        if (!Precision.equals(emaxY - eminY, 0, Precision.EPSILON)) {
            vscrollValue = (pivotLogicalCoords.getY() - eminY) / (emaxY - eminY);
        }
        onLogicalExtentUpdated();
    }


    private void onLogicalPivotUpdated() {
        refreshLogicalViewport();
    }


    private void onLogicalViewportUpdated() {
        refreshLogicalExtent();
    }


    private void onLogicalExtentUpdated() {
        // T = half viewport dimension (to center the view in viewport) - scaled pivot coord (because SCALE transform goes first, so translation works on already scaled contents)
        final double tx = (viewportLogicalBounds.getWidth() / 2.0) - (pivotLogicalCoords.getX() / scale);
        final double ty = (viewportLogicalBounds.getHeight() / 2.0) - (pivotLogicalCoords.getY() / scale);
        final double vah = viewportLogicalBounds.getWidth() / extentLogicalBounds.getWidth();
        final double vav = viewportLogicalBounds.getHeight() / extentLogicalBounds.getHeight();

        updatingScroll = true;
        try {
            view.setContentTranslation(tx, ty);
            view.setContentScalePivot(pivotLogicalCoords.getX(), pivotLogicalCoords.getY());
            view.setHScrollVisibleAmount(vah);
            view.setVScrollVisibleAmount(vav);
            view.setContentScaleFactor(scale);
            view.setHScrollValue(hscrollValue);
            view.setVScrollValue(vscrollValue);
        } finally {
            updatingScroll = false;
        }
    }


    private double checkRange0_inf(double value) {
        if (value < 0.00001d) {
            value = 0.00001d;
        }
        return value;
    }


    private double checkRange0_1(double value) {
        if (value < 0.0d) {
            value = 0.0d;
        }
        if (value > 1.0d) {
            value = 1.0d;
        }
        return value;
    }
}
