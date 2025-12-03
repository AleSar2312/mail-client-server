package prog3.Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ServerApplication.class.getResource("/prog3/Server/ServerView.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Mail Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}