package prog3.Client.Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import prog3.Client.MainView.ClientModel;
import prog3.Client.MainView.MainViewController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private Button loginBtn;
    @FXML private Label errorLbl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final String path = "src/main/java/prog3/Server/mailboxes/";

    @FXML
    private void onLogin() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Inserisci un'email");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Formato email non valido");
            return;
        }

        if (!Files.exists(Path.of(path + email))) {
            showError("Indirizzo email non esistente");
            return;
        }

        openMainView(email);
    }

    private void openMainView(String email) {
        try {
            Stage stage = new Stage();
            ClientModel model = new ClientModel(email);

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/MainView/MainView.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

            MainViewController controller = fxmlLoader.getController();
            controller.initModel(model, stage);

            stage.setTitle("Mail Client - " + email);
            stage.setScene(scene);
            stage.show();

            Stage loginStage = (Stage) loginBtn.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            showError("Errore apertura MainView");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLbl.setText(message);
        errorLbl.setVisible(true);
    }
}