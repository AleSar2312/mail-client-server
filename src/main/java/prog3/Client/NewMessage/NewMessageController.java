package prog3.Client.NewMessage;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class NewMessageController {

    @FXML private TextField txtTo;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtBody;
    @FXML private Label lblError;
    @FXML private Button btnSend;
    @FXML private Button btnCancel;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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

        System.out.println("Invio email a: " + to);
        System.out.println("Oggetto: " + subject);
        System.out.println("Messaggio: " + body);

        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void setReplyTo(String recipient, String subject) {
        txtTo.setText(recipient);
        txtTo.setEditable(false);
        txtSubject.setText(subject);
    }

    public void setForward(String subject, String originalBody) {
        txtSubject.setText(subject);
        txtBody.setText("\n\n--- Messaggio inoltrato ---\n" + originalBody);
    }
}