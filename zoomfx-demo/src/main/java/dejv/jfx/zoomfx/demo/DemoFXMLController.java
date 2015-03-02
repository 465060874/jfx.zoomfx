package dejv.jfx.zoomfx.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dejv.jfx.zoomfx.ZoomFX;

/**
 * FXML controller for fxml/demo.fxml
 * <br/>
 *
 * @author dejv78 (www.github.com/dejv78)
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
        zoomFX.zoomFactorProperty().addListener((prop, oldVal, newVal)-> zoomFactor.setText(String.format("%d%%", Math.round(newVal.doubleValue()*100))));
        bOne.setOnAction((event) -> zoomFX.zoomFactorProperty().set(1.0));
        bMinus.setOnAction((event) -> zoomFX.zoomFactorProperty().set(zoomFX.zoomFactorProperty().get() * 0.75));
        bPlus.setOnAction((event)->zoomFX.zoomFactorProperty().set(zoomFX.zoomFactorProperty().get()*1.25));
    }
}
