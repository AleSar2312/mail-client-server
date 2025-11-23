package prog3.Client.Common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Classe che rappresenta un'email
 */
public class Email implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String sender;
    private List<String> receivers;
    private String subject;
    private String text;
    private Date date;

    public Email(int id, String sender, List<String> receivers, String subject, String text, Date date) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.text = text;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Formato per salvare su file (separato da punto e virgola)
     */
    public String toFileFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return id + ";" + sender + ";" + String.join(",", receivers) + ";" +
                subject + ";" + text + ";" + sdf.format(date);
    }

    /**
     * Metodo per creare un'Email da una stringa in formato file
     */
    public static Email fromFileFormat(String line) {
        String[] parts = line.split(";");
        if (parts.length < 6) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0]);
            String sender = parts[1];
            List<String> receivers = List.of(parts[2].split(","));
            String subject = parts[3];
            String text = parts[4];
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = sdf.parse(parts[5]);

            return new Email(id, sender, receivers, subject, text, date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Rappresentazione per la ListView (mostra mittente e oggetto)
     */
    @Override
    public String toString() {
        return "Da: " + sender + " - " + subject;
    }
}
