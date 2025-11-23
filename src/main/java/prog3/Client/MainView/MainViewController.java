package prog3.Client.MainView;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import prog3.Client.Common.Email;
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

    private ClientModel model;

    @FXML
    private void initialize() {
        // Inizializzazione base
    }

    public void setUserEmail(String email) {
        this.model = new ClientModel(email);
        accountLbl.setText(email);
        inboxList.setItems(model.getMailList());

        inboxList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> showEmailDetails(newVal)
        );

        model.loadEmails();
        statusLbl.setText("Caricate " + model.getMailList().size() + " email");
    }

    private void showEmailDetails(Email email) {
        if (email == null) {
            lblFrom.setText("-");
            lblTo.setText("-");
            lblSubject.setText("-");
            lblDate.setText("-");
            txtBody.setText("");
            return;
        }

        lblFrom.setText(email.getSender());
        lblTo.setText(String.join(", ", email.getReceivers()));
        lblSubject.setText(email.getSubject());
        lblDate.setText(email.getDate().toString());
        txtBody.setText(email.getText());

        statusLbl.setText("Visualizzazione email: " + email.getSubject());
    }

    @FXML
    private void onNew() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/NewMessage/NewMessage.fxml")
            );
            Parent root = loader.load();

            NewMessageController controller = loader.getController();
            controller.setUserEmail(model.getAccount());

            Stage stage = new Stage();
            stage.setTitle("Nuovo Messaggio");
            stage.setScene(new Scene(root));
            stage.show();

            statusLbl.setText("Finestra nuovo messaggio aperta");
        } catch (IOException e) {
            statusLbl.setText("Errore apertura finestra messaggio");
            e.printStackTrace();
        }
    }

    @FXML
    private void onReply() {
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLbl.setText("Nessuna email selezionata");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/NewMessage/NewMessage.fxml")
            );
            Parent root = loader.load();

            NewMessageController controller = loader.getController();
            controller.setUserEmail(model.getAccount());
            controller.setReplyTo(selected.getSender(), "Re: " + selected.getSubject());

            Stage stage = new Stage();
            stage.setTitle("Rispondi");
            stage.setScene(new Scene(root));
            stage.show();

            statusLbl.setText("Risposta a: " + selected.getSender());
        } catch (IOException e) {
            statusLbl.setText("Errore apertura finestra risposta");
            e.printStackTrace();
        }
    }

    @FXML
    private void onReplyAll() {
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLbl.setText("Nessuna email selezionata");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/NewMessage/NewMessage.fxml")
            );
            Parent root = loader.load();

            NewMessageController controller = loader.getController();
            controller.setUserEmail(model.getAccount());

            // Rispondi a mittente + tutti i destinatari (escluso se stesso)
            String allRecipients = selected.getSender();
            for (String receiver : selected.getReceivers()) {
                if (!receiver.equals(model.getAccount())) {
                    allRecipients += "," + receiver;
                }
            }

            controller.setReplyTo(allRecipients, "Re: " + selected.getSubject());

            Stage stage = new Stage();
            stage.setTitle("Rispondi a tutti");
            stage.setScene(new Scene(root));
            stage.show();

            statusLbl.setText("Risposta a tutti");
        } catch (IOException e) {
            statusLbl.setText("Errore apertura finestra");
            e.printStackTrace();
        }
    }

    @FXML
    private void onForward() {
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLbl.setText("Nessuna email selezionata");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/prog3/Client/NewMessage/NewMessage.fxml")
            );
            Parent root = loader.load();

            NewMessageController controller = loader.getController();
            controller.setUserEmail(model.getAccount());
            controller.setForward("Fwd: " + selected.getSubject(), selected.getText());

            Stage stage = new Stage();
            stage.setTitle("Inoltra");
            stage.setScene(new Scene(root));
            stage.show();

            statusLbl.setText("Inoltro messaggio");
        } catch (IOException e) {
            statusLbl.setText("Errore apertura finestra");
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        Email selected = inboxList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            model.deleteEmail(selected);
            statusLbl.setText("Eliminata: " + selected.getSubject());
        } else {
            statusLbl.setText("Nessuna email selezionata");
        }
    }

    @FXML
    private void onRefresh() {
        model.loadEmails();
        statusLbl.setText("Posta aggiornata - " + model.getMailList().size() + " email");
    }
}