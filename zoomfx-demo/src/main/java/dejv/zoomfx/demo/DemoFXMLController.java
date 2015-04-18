package dejv.zoomfx.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dejv.commons.jfx.input.handler.DragActionHandler;
import dejv.commons.jfx.input.handler.ScrollActionHandler;
import dejv.jfx.zoomfx.ZoomFX;

/**
 * FXML controller for fxml/demo.fxml
 *
 * @author dejv78 (dejv78.github.io)
 */
public class DemoFXMLController {

    static final long serialVersionUID = 02L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoFXMLController.class);

    @FXML
    private AnchorPane pane;

    @FXML
    private ZoomFX zoomFX;

    @FXML
    private Button bOne;

    @FXML
    private Button bMinus;

    @FXML
    private Button bPlus;

    @FXML
    private Label zoomFactor;


    @FXML
    public void initialize() {
        zoomFX.zoomFactorProperty().addListener((prop, oldVal, newVal) -> zoomFactor.setText(String.format("%d%%", Math.round(newVal.doubleValue() * 100))));
        bOne.setOnAction((event) -> zoomFX.zoomFactorProperty().set(1.0));
        bMinus.setOnAction((event) -> zoomFX.zoomFactorProperty().set(zoomFX.zoomFactorProperty().get() * 0.75));
        bPlus.setOnAction((event) -> zoomFX.zoomFactorProperty().set(zoomFX.zoomFactorProperty().get() * 1.25));

        ScrollActionHandler.with(Demo.CONFIG.getZoomFXZoom())
                .doOnScroll((event) -> zoomFX.zoom(event.getDeltaY()))
                .register(zoomFX.getViewport());

        DragActionHandler.with(Demo.CONFIG.getZoomFXPan())
                .doOnDragStart((event) -> zoomFX.startPan(event.getSceneX(), event.getSceneY()))
                .doOnDrag((event) -> zoomFX.pan(event.getSceneX(), event.getSceneY()))
                .doOnDragFinish((event) -> zoomFX.endPan())
                .register(zoomFX.getViewport());
    }
}
