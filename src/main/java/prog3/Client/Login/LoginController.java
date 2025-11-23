package prog3.Client.Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import prog3.Client.MainView.MainViewController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private Button loginBtn;
    @FXML private Label errorLbl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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

        // Connessione al server
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Invia comando LOGIN
            out.println("LOGIN:" + email);

            // Aspetta risposta
            String response = in.readLine();

            if (response.startsWith("OK")) {
                // Login riuscito - apri MainView
                openMainView(email);
            } else {
                showError("Utente non trovato. Account non esistente.");
            }

            socket.close();

        } catch (IOException e) {
            showError("Impossibile connettersi al server");
            e.printStackTrace();
        }
    }

    private void openMainView(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/MainView/MainView.fxml")
            );
            Parent root = loader.load();

            MainViewController controller = loader.getController();
            controller.setUserEmail(email);

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mail Client - " + email);
            stage.show();

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