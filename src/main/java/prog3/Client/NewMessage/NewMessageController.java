package prog3.Client.NewMessage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class NewMessageController {

    @FXML private TextField txtTo;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtBody;
    @FXML private Label errorLbl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void setReplyTo(String to, String subject) {
        txtTo.setText(to);
        txtSubject.setText(subject);
    }

    public void setForward(String subject, String body) {
        txtSubject.setText(subject);
        txtBody.setText("\n\n--- Messaggio inoltrato ---\n" + body);
    }

    @FXML
    private void onSend() {
        String to = txtTo.getText().trim();
        String subject = txtSubject.getText().trim();
        String body = txtBody.getText().trim();

        if (to.isEmpty()) {
            showError("Inserisci almeno un destinatario");
            return;
        }

        if (!EMAIL_PATTERN.matcher(to).matches()) {
            showError("Formato email non valido");
            return;
        }

        if (subject.isEmpty()) {
            showError("Inserisci un oggetto");
            return;
        }

        if (body.isEmpty()) {
            showError("Inserisci un messaggio");
            return;
        }

        // Invio al server
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Formato: SEND_EMAIL:from:to:subject:body
            String message = "SEND_EMAIL:" + userEmail + ":" + to + ":" + subject + ":" + body;
            out.println(message);

            String response = in.readLine();

            if (response.startsWith("OK")) {
                System.out.println("Email inviata con successo");
                closeWindow();
            } else {
                showError("Errore invio email");
            }

            socket.close();

        } catch (IOException e) {
            showError("Impossibile connettersi al server");
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtTo.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLbl.setText(message);
        errorLbl.setVisible(true);
    }
}