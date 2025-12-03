package prog3.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import prog3.Common.MailString;

public class AllMailBoxes {

    private HashMap<String, Mailbox> all_mailboxes = new HashMap<>();
    private static final String path = "src/main/java/prog3/Server/mailboxes/";

    public void initMailbox(String user) {
        if (!all_mailboxes.containsKey(user)) {
            System.out.println("Creo nuova Mailbox per " + user);
            all_mailboxes.put(user, new Mailbox(user));
            all_mailboxes.get(user).initialize();
        } else {
            System.out.println("Mailbox già esistente per " + user);
        }
    }

    public ArrayList<MailString> getInitMailList(String user) {
        if (!all_mailboxes.containsKey(user))
            initMailbox(user);

        all_mailboxes.get(user).new_mail.clear();
        return new ArrayList<>(all_mailboxes.get(user).mail_list.values());
    }

    public boolean askNew(String user) {
        if (!all_mailboxes.containsKey(user))
            initMailbox(user);
        return !all_mailboxes.get(user).new_mail.isEmpty();
    }

    public ArrayList<MailString> getNew(String user) {
        if (!all_mailboxes.containsKey(user))
            initMailbox(user);

        ArrayList<MailString> newsList = new ArrayList<>(all_mailboxes.get(user).new_mail);
        all_mailboxes.get(user).new_mail.clear();
        return newsList;
    }

    public boolean userExists(String email) {
        return Files.exists(Path.of(path + email));
    }

    public String sendMail(String user, MailString mail_to_send) {
        if (!all_mailboxes.containsKey(user))
            initMailbox(user);
        return all_mailboxes.get(user).send(mail_to_send);
    }

    public void deleteMail(int mail_id, String user) {
        System.out.println("DEBUG AllMailBoxes.deleteMail: ID=" + mail_id + ", User=" + user);
        System.out.println("all_mailboxes contiene " + user + "? " + all_mailboxes.containsKey(user));

        if (!all_mailboxes.containsKey(user)) {
            System.out.println("Inizializzo mailbox per " + user);
            initMailbox(user);
        }

        Mailbox mailbox = all_mailboxes.get(user);
        System.out.println("Mailbox ottenuta: " + (mailbox != null ? "OK" : "NULL"));

        if (mailbox != null) {
            mailbox.delete(mail_id);
            System.out.println("Chiamato delete() sulla mailbox");
        } else {
            System.out.println("ERRORE: Mailbox è NULL!");
        }
    }

    // ==================== INNER CLASS MAILBOX ====================

    private class Mailbox {
        private HashMap<String, MailString> mail_list = new HashMap<>();
        private ArrayList<MailString> new_mail = new ArrayList<>();
        private final String user;

        private Mailbox(String user) {
            this.user = user;
        }

        private void initialize() {
            if (!mail_list.isEmpty()) {
                System.out.println("SKIP initialize - Mailbox già popolata con " + mail_list.size() + " email");
                return;
            }

            File inboxFile = new File(path + user + "/inbox.txt");

            if (!inboxFile.exists()) {
                inboxFile.getParentFile().mkdirs();
                System.out.println("File inbox.txt non esiste per " + user);
                return;
            }

            System.out.println("CARICO email da file per " + user);

            try (BufferedReader reader = new BufferedReader(new FileReader(inboxFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 6) {
                        int id = Integer.parseInt(parts[0]);
                        MailString mail = new MailString(
                                id,
                                parts[1],
                                parts[2],
                                parts[3],
                                parts[4],
                                parts[5]
                        );
                        mail_list.put(parts[0], mail);
                    }
                }
                System.out.println("✓ Caricate " + mail_list.size() + " email dal file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String send(MailString mail_to_send) {
            String receivers_string = mail_to_send.getDestinatari().replaceAll("\\s+", "");
            String[] receivers = receivers_string.split(",");

            for (String receiver : receivers) {
                if (receiver.isBlank() || receiver.isEmpty()) {
                    return "Inserire destinatario/i";
                }
                if (!Files.exists(Path.of(path + receiver))) {
                    return "Inserire un indirizzo mail esistente";
                }
            }

            for (String receiver : receivers) {
                initMailbox(receiver);

                int id;
                if (all_mailboxes.get(receiver).mail_list.isEmpty()) {
                    id = 0;
                } else {
                    id = all_mailboxes.get(receiver).mail_list.keySet().stream()
                            .mapToInt(Integer::parseInt)
                            .max()
                            .orElse(-1) + 1;
                }

                mail_to_send.setId(id);

                File inboxFile = new File(path + receiver + "/inbox.txt");
                try (PrintWriter writer = new PrintWriter(new FileWriter(inboxFile, true))) {
                    String emailLine = id + ";" +
                            mail_to_send.getMittente() + ";" +
                            receiver + ";" +
                            mail_to_send.getOggetto() + ";" +
                            mail_to_send.getCorpo() + ";" +
                            mail_to_send.getData();
                    writer.println(emailLine);
                } catch (IOException e) {
                    return "Errore salvataggio";
                }

                all_mailboxes.get(receiver).mail_list.put(Integer.toString(id), mail_to_send);
                all_mailboxes.get(receiver).new_mail.add(mail_to_send);
            }

            return "Email inviata";
        }

        private void delete(int mail_id) {
            System.out.println("=== DELETE DEBUG ===");
            System.out.println("User: " + user);
            System.out.println("Mail ID da eliminare: " + mail_id);
            System.out.println("Mail_list keys disponibili: " + mail_list.keySet());
            System.out.println("Mail_list size PRIMA: " + mail_list.size());

            MailString removed = mail_list.remove(Integer.toString(mail_id));

            if (removed != null) {
                System.out.println("✓ Email rimossa dalla HashMap: " + removed.getOggetto());
            } else {
                System.out.println("✗ ERRORE: Email con ID " + mail_id + " NON TROVATA!");
                return;
            }

            System.out.println("Mail_list size DOPO: " + mail_list.size());

            File inboxFile = new File(path + user + "/inbox.txt");
            System.out.println("Path file: " + inboxFile.getAbsolutePath());

            try (PrintWriter writer = new PrintWriter(new FileWriter(inboxFile))) {
                int count = 0;
                for (MailString mail : mail_list.values()) {
                    String emailLine = mail.getId() + ";" +
                            mail.getMittente() + ";" +
                            mail.getDestinatari() + ";" +
                            mail.getOggetto() + ";" +
                            mail.getCorpo() + ";" +
                            mail.getData();
                    writer.println(emailLine);
                    count++;
                    System.out.println("  Scritta email ID " + mail.getId() + ": " + mail.getOggetto());
                }
                System.out.println("✓ File riscritto con " + count + " email");
            } catch (IOException e) {
                System.out.println("✗ ERRORE scrittura file: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("===================\n");
        }
    }
}