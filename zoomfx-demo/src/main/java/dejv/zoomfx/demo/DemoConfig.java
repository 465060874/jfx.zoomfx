package dejv.zoomfx.demo;

import dejv.commons.jfx.input.Buttons;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.mouse.MouseGestureEventProperties;

/**
 * <br/>
 *
 * @author dejv78 (dejv78.github.io)
 */
public class DemoConfig {

    private GestureEventProperties zoomFXZoom = new GestureEventProperties(null);
    private MouseGestureEventProperties zoomFXPan = new MouseGestureEventProperties(null, new Buttons().withMiddle());


    public GestureEventProperties getZoomFXZoom() {
        return zoomFXZoom;
    }


    public void setZoomFXZoom(GestureEventProperties zoomFXZoom) {
        this.zoomFXZoom = zoomFXZoom;
    }


    public MouseGestureEventProperties getZoomFXPan() {
        return zoomFXPan;
    }


    public void setZoomFXPan(MouseGestureEventProperties zoomFXPan) {
        this.zoomFXPan = zoomFXPan;
    }
}
