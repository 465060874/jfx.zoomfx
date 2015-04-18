package dejv.zoomfx.demo;

import dejv.commons.jfx.input.GestureModifiers;
import dejv.commons.jfx.input.MouseButtons;
import dejv.commons.jfx.input.MouseModifiers;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.MouseEventProperties;

/**
 * <br/>
 *
 * @author dejv78 (dejv78.github.io)
 */
public class DemoConfig {

    private GestureEventProperties zoomFXZoom = new GestureEventProperties(GestureModifiers.none());
    private MouseEventProperties zoomFXPan = new MouseEventProperties(MouseModifiers.none(), MouseButtons.middle());


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
