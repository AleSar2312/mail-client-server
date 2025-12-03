package prog3.Server;

import prog3.Common.MailString;
import prog3.Common.Operations;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServerModel serverModel;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private boolean isInitialLogin = false;

    public ClientHandler(Socket socket, ServerModel serverModel) {
        this.socket = socket;
        this.serverModel = serverModel;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            username = (String) in.readObject();
            serverModel.getLog().addEntry("L'utente " + username + " si è connesso");

            String operation = (String) in.readObject();
            serverModel.getLog().addEntry(username + " - Operazione: " + operation);

            if (operation.equals(Operations.initMailbox)) {
                isInitialLogin = true;
                serverModel.getController().incrementConnections();
            }

            handleOperation(operation);

        } catch (IOException | ClassNotFoundException e) {
            serverModel.getLog().addEntry("Errore comunicazione: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void handleOperation(String operation) throws IOException, ClassNotFoundException {
        System.out.println("=== HANDLE OPERATION ===");
        System.out.println("Operazione: '" + operation + "'");

        if (operation.equals(Operations.initMailbox)) {
            System.out.println("→ initMailbox");
            handleInitMailbox();

        } else if (operation.equals(Operations.send)) {
            System.out.println("→ send");
            handleSendEmail();

        } else if (operation.equals(Operations.delete)) {  // ✅ CORRETTO!
            System.out.println("→ DELETE");
            handleDeleteEmail();

        } else if (operation.equals(Operations.checkNew)) {
            System.out.println("→ checkNew");
            handleCheckNew();

        } else if (operation.equals(Operations.getNew)) {
            System.out.println("→ getNew");
            handleGetNew();

        } else {
            System.out.println("→ SCONOSCIUTA: '" + operation + "'");
            serverModel.getLog().addEntry("Operazione sconosciuta: '" + operation + "'");
        }
    }

    private void handleInitMailbox() throws IOException {
        if (!serverModel.getMailBoxes().userExists(username)) {
            out.writeObject("ERROR:Utente non trovato");
            out.flush();
            serverModel.getLog().addEntry("Login fallito per: " + username);
        } else {
            ArrayList<MailString> emails = serverModel.getMailBoxes().getInitMailList(username);
            out.writeObject(emails);
            out.flush();
            serverModel.getLog().addEntry("Login riuscito per: " + username + " - Inviate " + emails.size() + " email");
        }
    }

    private void handleSendEmail() throws IOException, ClassNotFoundException {
        MailString mail = (MailString) in.readObject();
        String result = serverModel.getMailBoxes().sendMail(username, mail);

        System.out.println("DEBUG Server: Invio risposta '" + result + "'");

        out.writeObject(result);
        out.flush();

        serverModel.getLog().addEntry("Email da " + username + ": " + result);
    }

    private void handleDeleteEmail() throws IOException, ClassNotFoundException {
        System.out.println("=== HANDLER DELETE EMAIL ===");

        try {
            System.out.println("1. Leggo l'ID dall'input stream...");
            int emailId = (int) in.readObject();

            System.out.println("2. ID ricevuto: " + emailId);
            System.out.println("3. Username: " + username);
            System.out.println("4. Chiamo AllMailBoxes.deleteMail()...");

            serverModel.getMailBoxes().deleteMail(emailId, username);

            System.out.println("5. deleteMail() completato");
            System.out.println("6. Invio risposta OK...");

            out.writeObject("OK:Email eliminata");
            out.flush();

            System.out.println("7. Risposta inviata");

            serverModel.getLog().addEntry("Eliminata email ID " + emailId + " da " + username);

            System.out.println("8. Log aggiornato");
            System.out.println("============================\n");

        } catch (Exception e) {
            System.out.println("ERRORE in handleDeleteEmail:");
            System.out.println("Tipo errore: " + e.getClass().getName());
            System.out.println("Messaggio: " + e.getMessage());
            e.printStackTrace();

            serverModel.getLog().addEntry("ERRORE eliminazione: " + e.getMessage());

            out.writeObject("ERROR:Errore eliminazione");
            out.flush();
        }
    }

    private void handleCheckNew() throws IOException {
        boolean hasNew = serverModel.getMailBoxes().askNew(username);
        out.writeObject(hasNew);
        out.flush();

        if (hasNew) {
            serverModel.getLog().addEntry("📧 Nuove mail per " + username);
        }
    }

    private void handleGetNew() throws IOException {
        ArrayList<MailString> newMails = serverModel.getMailBoxes().getNew(username);
        out.writeObject(newMails);
        out.flush();
        serverModel.getLog().addEntry("Inviate " + newMails.size() + " nuove email a " + username);
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) in.close();
            if (out != null) out.close();

            if (isInitialLogin) {
                serverModel.getController().decrementConnections();
            }

            if (username != null) {
                serverModel.getLog().addEntry("L'utente " + username + " si è disconnesso");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}