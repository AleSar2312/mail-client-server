package prog3.Client.NewMessage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog3.Common.Email;
import prog3.Common.MailString;
import prog3.Common.Operations;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class NewMessageController {

    @FXML private Button btnSend;        // ✅ Cambiato da sendBtn
    @FXML private TextArea txtBody;
    @FXML private TextField txtSubject;
    @FXML private TextField txtTo;
    @FXML private Label lblError;        // ✅ Cambiato da errorLbl
    @FXML private Button btnCancel;      // ✅ Cambiato da cancelBtn

    private String userEmail;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public void initModel(String userEmail, String btnType, Email selected) {
        this.userEmail = userEmail;

        if (selected != null) {
            switch (btnType) {
                case "forwardBtn":
                    txtBody.setText(selected.getCorpo());
                    txtSubject.setText("Fwd: " + selected.getOggetto());
                    break;
                case "replyBtn":
                    txtTo.setText(selected.getMittente());
                    txtSubject.setText("Re: " + selected.getOggetto());
                    break;
                case "replyAllBtn":
                    StringBuilder replyAll = new StringBuilder(selected.getMittente());
                    String[] receivers = selected.getDestinatari().replaceAll("\\s+", "").split(",");
                    for (String receiver : receivers) {
                        if (!receiver.equals(userEmail)) {
                            replyAll.append(", ").append(receiver);
                        }
                    }
                    txtTo.setText(replyAll.toString());
                    txtSubject.setText("Re: " + selected.getOggetto());
                    break;
            }
        }
    }

    @FXML
    private void onSend() {
        String to = txtTo.getText().trim();
        String subject = txtSubject.getText().trim();
        String body = txtBody.getText().trim();

        if (to.isEmpty()) {
            showError("Inserisci destinatario/i");
            return;
        }

        if (!isValid(to)) {
            showError("Formato email non valido");
            return;
        }

        if (subject.isEmpty() && body.isEmpty()) {
            showError("Impossibile inviare messaggio vuoto");
            return;
        }

        // ✅ Ora btnCancel funziona perché corrisponde a fx:id="btnCancel" nell'FXML
        final Stage currentStage = (Stage) btnCancel.getScene().getWindow();

        new Thread(() -> {
            try {
                System.out.println("\n=== INVIO EMAIL ===");
                System.out.println("Da: " + userEmail);
                System.out.println("A: " + to);

                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                out.writeObject(userEmail);
                out.writeObject(Operations.send);
                out.flush();

                MailString mail = new MailString(
                        userEmail,
                        to,
                        subject,
                        body,
                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime())
                );

                out.writeObject(mail);
                out.flush();

                System.out.println("Email inviata al server, attendo risposta...");

                String response = (String) in.readObject();

                System.out.println("RISPOSTA SERVER: '" + response + "'");

                in.close();
                out.close();
                socket.close();

                if (response != null && response.equals("Email inviata")) {
                    System.out.println("✅ SUCCESS - Chiudo finestra");
                    Platform.runLater(() -> {
                        if (currentStage != null) {
                            currentStage.close();
                            System.out.println("✅ Finestra chiusa!");
                        }
                    });
                } else {
                    System.out.println("❌ ERROR: " + response);
                    Platform.runLater(() -> showError(response != null ? response : "Errore invio"));
                }

            } catch (Exception e) {
                System.out.println("❌ ECCEZIONE: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> showError("Errore connessione server"));
            }
        }).start();
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        if (lblError != null) {  // ✅ Cambiato da errorLbl
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    private Boolean isValid(String destinatari) {
        String[] array = destinatari.replaceAll("\\s+", "").split(",");
        for (String d : array) {
            if (!EMAIL_PATTERN.matcher(d).matches())
                return false;
        }
        return true;
    }
}