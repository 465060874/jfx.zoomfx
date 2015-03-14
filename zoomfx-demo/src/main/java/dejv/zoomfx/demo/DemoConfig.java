package dejv.zoomfx.demo;

import dejv.commons.jfx.input.MouseButtons;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.MouseEventProperties;

/**
 * <br/>
 *
 * @author dejv78 (dejv78.github.io)
 */
public class DemoConfig {

    private GestureEventProperties zoomFXZoom = new GestureEventProperties(null);
    private MouseEventProperties zoomFXPan = new MouseEventProperties(null, new MouseButtons().withMiddle());


    public GestureEventProperties getZoomFXZoom() {
        return zoomFXZoom;
    }


    public void setZoomFXZoom(GestureEventProperties zoomFXZoom) {
        this.zoomFXZoom = zoomFXZoom;
    }


    public MouseEventProperties getZoomFXPan() {
        return zoomFXPan;
    }


    public void setZoomFXPan(MouseEventProperties zoomFXPan) {
        this.zoomFXPan = zoomFXPan;
    }
}
