package prog3.Client.MainView;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import prog3.Common.Email;
import prog3.Common.MailString;
import prog3.Common.Operations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientModel {
    private final SimpleStringProperty account = new SimpleStringProperty();
    private final SimpleStringProperty warning = new SimpleStringProperty();
    private final SimpleBooleanProperty notify = new SimpleBooleanProperty();
    private ObservableList<Email> mailList = FXCollections.observableArrayList();
    private final ObjectProperty<Email> currentMail = new SimpleObjectProperty<>();
    private boolean newMail = false;
    private boolean init_mailBox = false;
    private boolean init_server_disconnected = false;
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    public ClientModel(String account) {
        this.account.set(account);
        newMail = false;

        Thread clientThread = new Thread(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        if (!init_mailBox) {
                            getInitMailBox();
                        } else {
                            if (init_server_disconnected) {
                                getInitMailBox();
                            } else {
                                askForNewMails();
                            }
                        }
                    });
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.start();
    }

    public Email getCurrentMail() {
        return currentMail.get();
    }

    public ObjectProperty<Email> currentMailProperty() {
        return currentMail;
    }

    public String getAccount() {
        return account.get();
    }

    public SimpleStringProperty accountProperty() {
        return account;
    }

    public String getWarning() {
        return warning.get();
    }

    public SimpleStringProperty warningProperty() {
        return warning;
    }

    public boolean isNotify() {
        return notify.get();
    }

    public SimpleBooleanProperty notifyProperty() {
        return notify;
    }

    public ObservableList<Email> getMailList() {
        return mailList;
    }

    private Socket connecting() {
        try {
            Socket s = new Socket("localhost", 8080);
            warning.setValue("");
            return s;
        } catch (IOException e) {
            warning.set("Server non attivo");
        }
        return null;
    }

    private void getInitMailBox() {
        Socket socket = connecting();
        if (socket != null) {
            executorService.execute(new OperationsHandler(socket, "getInitMailBox", -1));
        }
    }

    protected void askForNewMails() {
        if (!init_mailBox || init_server_disconnected) {
            getInitMailBox();
        } else {
            Socket socket = connecting();
            if (socket != null) {
                executorService.execute(new OperationsHandler(socket, "askForNewMails", -1));
            } else {
                if (init_mailBox)
                    init_server_disconnected = true;
            }
        }
    }

    private void getNewMails() {
        if (newMail) {
            Socket socket = connecting();
            if (socket != null) {
                executorService.execute(new OperationsHandler(socket, "getNewMails", -1));
            }
        }
    }

    protected synchronized void deleteMail() {
        Email current = getCurrentMail();
        if (current == null) {
            System.out.println("DEBUG: Nessuna email selezionata");
            warning.set("Nessuna email selezionata");
            return;
        }

        int emailId = current.getId();
        System.out.println("=== DEBUG CLIENT ELIMINAZIONE ===");
        System.out.println("Email da eliminare: ID=" + emailId);
        System.out.println("Oggetto: " + current.getOggetto());
        System.out.println("Size lista PRIMA: " + mailList.size());

        // Rimuovi dalla lista UI
        boolean removed = mailList.remove(current);
        System.out.println("Rimossa dalla lista UI? " + removed);
        System.out.println("Size lista DOPO: " + mailList.size());

        // Notifica il server
        Socket socket = connecting();
        if (socket != null) {
            System.out.println("Socket connesso, invio richiesta delete al server...");
            executorService.execute(new OperationsHandler(socket, "deleteMail", emailId));
        } else {
            System.out.println("ERRORE: Socket è null!");
        }

        System.out.println("=================================\n");
    }

    private synchronized void updateList(List<MailString> mailBox) {
        if (init_server_disconnected) {
            mailList.clear();
        }

        for (MailString mail : mailBox) {
            Email email = new Email(
                    mail.getId(),
                    mail.getMittente(),
                    mail.getDestinatari(),
                    mail.getOggetto(),
                    mail.getCorpo(),
                    mail.getData()
            );
            mailList.add(0, email);
        }
        notify.setValue(false);
        init_server_disconnected = false;
    }

    public class OperationsHandler implements Runnable {
        private int id;
        private final String op_type;
        private final Socket socket;

        public OperationsHandler(Socket socket, String op_type, int id) {
            this.op_type = op_type;
            this.socket = socket;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(getAccount());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    switch (op_type) {
                        case "getInitMailBox":
                            outputStream.writeObject(Operations.initMailbox);
                            outputStream.flush();

                            Object response = inputStream.readObject();
                            if (response instanceof String && ((String) response).startsWith("ERROR")) {
                                Platform.runLater(() -> warning.set("Utente non trovato"));
                            } else {
                                List<MailString> mailList = (List<MailString>) response;
                                if (mailList != null) {
                                    Platform.runLater(() -> updateList(mailList));
                                    init_mailBox = true;
                                }
                            }
                            break;

                        case "askForNewMails":
                            outputStream.writeObject(Operations.checkNew);
                            outputStream.flush();

                            newMail = (boolean) inputStream.readObject();
                            Platform.runLater(() -> notify.set(newMail));
                            getNewMails();
                            break;

                        case "getNewMails":
                            outputStream.writeObject(Operations.getNew);
                            outputStream.flush();

                            List<MailString> newsList = (List<MailString>) inputStream.readObject();
                            Platform.runLater(() -> updateList(newsList));
                            newMail = false;
                            break;

                        case "deleteMail":
                            System.out.println("=== DEBUG OperationsHandler DELETE ===");
                            System.out.println("1. Invio Operations.delete: '" + Operations.delete + "'");

                            outputStream.writeObject(Operations.delete);
                            outputStream.flush();

                            System.out.println("2. Invio ID: " + id);
                            outputStream.writeObject(id);
                            outputStream.flush();

                            System.out.println("3. Attendo che il server completi...");
                            Thread.sleep(1000); // Aspetta 1 secondo

                            System.out.println("4. DELETE completato");
                            System.out.println("======================================\n");
                            break;
                    }

                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    Platform.runLater(() -> warning.setValue(e.getMessage()));
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            } catch (IOException e) {
                Platform.runLater(() -> warning.setValue(e.getMessage()));
            }
        }
    }
}