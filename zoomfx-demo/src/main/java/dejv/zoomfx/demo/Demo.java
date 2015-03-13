package dejv.zoomfx.demo;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import dejv.commons.config.ConfigException;
import dejv.commons.config.ConfigProvider;
import dejv.zoomfx.demo.beans.Config;

/**
 * ZoomFX demo application
 * <br/>
 *
 * @author dejv78 (dejv78.github.io)
 */
public class Demo extends Application {

    public static final AnnotationConfigApplicationContext APPLICATION_CONTEXT = new AnnotationConfigApplicationContext(Config.class);
    public static DemoConfig CONFIG = null;


    public static void main(final String[] args) {

        Application.launch(Demo.class, (String[]) null);
    }


    public ConfigProvider configProvider;


    @Override
    public void start(final Stage primaryStage) throws Exception {

        try {
            initConfig();

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


    @Override
    public void stop() throws Exception {
        super.stop();
        configProvider.store(CONFIG);
    }


    private void initConfig() {

        configProvider = APPLICATION_CONTEXT.getBean(ConfigProvider.class);

        try {
            CONFIG = configProvider.load(DemoConfig.class);
        } catch (ConfigException e) {
            CONFIG = new DemoConfig();
        }
    }
}

