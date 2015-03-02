package dejv.jfx.zoomfx.demo;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ZoomFX demo application
 * <br/>
 *
 * @author dejv78 (www.github.com/dejv78)
 */
public class Demo extends Application {

    public static final ApplicationContext APPLICATION_CONTEXT = new ClassPathXmlApplicationContext("demo.xml");

    public static void main(final String[] args) {
        Application.launch(Demo.class, (String[]) null);
    }


    @Override
    public void start(final Stage primaryStage) throws Exception {
        try {

            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(Demo.class.getResource("/fxml/demo.fxml"));

            final Pane page = fxmlLoader.load();
            final Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("ZoomFX demo");
            primaryStage.setWidth(800);
            primaryStage.setHeight(800);
            primaryStage.show();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}

