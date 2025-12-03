package prog3.Client.NewMessage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog3.Common.Email;
import prog3.Common.MailString;
import prog3.Common.Operations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class NewMessageController {

    @FXML private Button sendBtn;
    @FXML private TextArea txtBody;
    @FXML private TextField txtSubject;
    @FXML private TextField txtTo;
    @FXML private Label errorLbl;
    @FXML private Button cancelBtn;

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

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            System.out.println("\n=== INVIO EMAIL ===");
            System.out.println("Da: " + userEmail);
            System.out.println("A: " + to);

            socket = new Socket("localhost", 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

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
            System.out.println("===================\n");

            // Chiudi tutto PRIMA di decidere
            in.close();
            out.close();
            socket.close();

            // Controlla risposta
            if (response != null && response.equals("Email inviata")) {
                System.out.println("SUCCESS - Chiudo finestra");
                Stage stage = (Stage) sendBtn.getScene().getWindow();
                stage.close();
            } else {
                System.out.println("ERROR - Mostro errore");
                showError(response != null ? response : "Errore invio");
            }

        } catch (Exception e) {
            System.out.println("ECCEZIONE: " + e.getMessage());
            e.printStackTrace();
            showError("Errore connessione server");
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        if (errorLbl != null) {
            errorLbl.setText(message);
            errorLbl.setVisible(true);
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