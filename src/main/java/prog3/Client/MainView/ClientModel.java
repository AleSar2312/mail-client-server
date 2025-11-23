package prog3.Client.MainView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import prog3.Client.Common.Email;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientModel {

    private String account;
    private final ObservableList<Email> mailList;
    private File mailboxFile;
    private String email;

    public ClientModel(String email) {
        this.account = email;
        this.email = email;
        this.mailList = FXCollections.observableArrayList();
        this.mailboxFile = new File("src/main/java/prog3/Server/mailboxes/" + email + "/inbox.txt");

        File mailboxDir = new File("src/main/java/prog3/Server/mailboxes/" + email);
        if (!mailboxDir.exists()) {
            mailboxDir.mkdirs();
        }

        loadEmails();
    }

    public void setUserEmail(String email) {
        this.email = email;
        this.account = email;
        this.mailboxFile = new File("src/main/java/prog3/Server/mailboxes/" + email + "/inbox.txt");
        loadEmails();
    }

    public String getAccount() {
        return account;
    }

    public void loadEmails() {
        mailList.clear();
        if (!mailboxFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mailboxFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Email email = parseEmail(line);
                if (email != null) {
                    mailList.add(email);
                }
            }
            System.out.println("Caricate " + mailList.size() + " email per " + account);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Email parseEmail(String line) {
        String[] parts = line.split(";");
        if (parts.length >= 6) {
            int id = Integer.parseInt(parts[0]);
            String sender = parts[1];
            String receiver = parts[2];
            String subject = parts[3];
            String text = parts[4];
            String dateStr = parts[5];

            List<String> receivers = new ArrayList<>();
            receivers.add(receiver);

            return new Email(id, sender, receivers, subject, text, new Date());
        }
        return null;
    }

    public ObservableList<Email> getMailList() {
        return mailList;
    }

    public void deleteEmail(Email email) {
        mailList.remove(email);
        saveEmails();

        // Notifica il server
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message = "DELETE_EMAIL:" + this.account + ":" + email.getId();
            out.println(message);

            in.readLine(); // Ricevi risposta
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveEmails() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(mailboxFile))) {
            for (Email email : mailList) {
                writer.println(emailToString(email));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String emailToString(Email email) {
        return email.getId() + ";" +
                email.getSender() + ";" +
                String.join(",", email.getReceivers()) + ";" +
                email.getSubject() + ";" +
                email.getText() + ";" +
                email.getDate();
    }
}