package prog3.Client.MainView;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog3.Common.Email;
import prog3.Client.NewMessage.NewMessageController;

import java.io.IOException;

public class MainViewController {

    @FXML private Label accountLbl;
    @FXML private ListView<Email> inboxList;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblSubject;
    @FXML private Label lblDate;
    @FXML private TextArea txtBody;
    @FXML private Label statusLbl;
    @FXML private Button btnDelete;

    private ClientModel model;

    public void initModel(ClientModel model, Stage stage) {
        if (this.model != null) {
            throw new IllegalStateException("Il modello può essere inizializzato solo una volta");
        }
        this.model = model;

        accountLbl.textProperty().bind(model.accountProperty());
        inboxList.setItems(model.getMailList());
        statusLbl.textProperty().bind(model.warningProperty());

        model.notifyProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hai ricevuto una nuova mail!");
                alert.initOwner(stage);
                alert.setTitle("Notifica");
                alert.setHeaderText(accountLbl.getText());
                alert.show();
            }
        });

        inboxList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            showEmailDetails(newV);
            model.currentMailProperty().set(newV);  // ✅ AGGIUNGI QUESTA RIGA!
        });
    }

    private void showEmailDetails(Email email) {
        if (email == null) {
            lblFrom.setText("-");
            lblTo.setText("-");
            lblSubject.setText("-");
            txtBody.setText("");
            lblDate.setText("-");
        } else {
            lblFrom.setText(email.getMittente());
            lblTo.setText(email.getDestinatari());
            lblSubject.setText(email.getOggetto());
            txtBody.setText(email.getCorpo());
            lblDate.setText(email.getData());
        }
    }

    @FXML
    private void onRefresh() {
        System.out.println("Refresh cliccato");
        model.askForNewMails();
    }

    @FXML
    private void onNew() {
        System.out.println("Nuovo messaggio");
        openMessageWindow("newMessageBtn");
    }

    @FXML
    private void onReply() {
        System.out.println("Rispondi");
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMessageWindow("replyBtn");
        }
    }

    @FXML
    private void onReplyAll() {
        System.out.println("Rispondi a tutti");
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMessageWindow("replyAllBtn");
        }
    }

    @FXML
    private void onForward() {
        System.out.println("Inoltra");
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMessageWindow("forwardBtn");
        }
    }

    @FXML
    private void onDelete() {
        System.out.println("=== BOTTONE ELIMINA CLICCATO ===");

        Email selected = inboxList.getSelectionModel().getSelectedItem();
        System.out.println("Email selezionata: " + (selected != null ? selected.getOggetto() : "NESSUNA"));

        if (selected != null) {
            System.out.println("Chiamo model.deleteMail()...");
            model.deleteMail();
            inboxList.getSelectionModel().clearSelection();
            System.out.println("Eliminazione completata");
            System.out.println("================================\n");
        } else {
            System.out.println("Nessuna email selezionata!");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona un'email da eliminare");
            alert.show();
        }
    }

    private void openMessageWindow(String btnType) {
        try {
            Stage newStage = new Stage();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/NewMessage/NewMessage.fxml")
            );
            Scene scene = new Scene(loader.load(), 600, 500);

            NewMessageController controller = loader.getController();
            Email selected = inboxList.getSelectionModel().getSelectedItem();
            controller.initModel(model.getAccount(), btnType, selected);

            newStage.setTitle("Nuovo Messaggio");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            System.out.println("ERRORE apertura finestra: " + e.getMessage());
            e.printStackTrace();
        }
    }
}