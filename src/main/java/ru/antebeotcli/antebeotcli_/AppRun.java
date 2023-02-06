package ru.antebeotcli.antebeotcli_;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.antebeotcli.antebeotcli_.AnteBeotWeb.JsonGetter;

import java.io.IOException;


public class AppRun extends Application {
    public static JsonGetter MainJsonGetter;

    static {
        try {
            MainJsonGetter = new JsonGetter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppRun.class.getResource("authorization.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("AntebeotCli");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        }); // override handle

    }

    public static void main(String[] args) {
        launch();
    }
}