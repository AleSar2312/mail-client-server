package prog3.Common;

import java.io.Serializable;

public class MailString implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String mittente;
    private String destinatari;
    private String oggetto;
    private String corpo;
    private String data;

    public MailString(int id, String mittente, String destinatari, String oggetto, String corpo, String data) {
        this.id = id;
        this.mittente = mittente;
        this.destinatari = destinatari;
        this.oggetto = oggetto;
        this.corpo = corpo;
        this.data = data;
    }

    public MailString(String mittente, String destinatari, String oggetto, String corpo, String data) {
        this.mittente = mittente;
        this.destinatari = destinatari;
        this.oggetto = oggetto;
        this.corpo = corpo;
        this.data = data;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMittente() { return mittente; }
    public String getDestinatari() { return destinatari; }
    public String getOggetto() { return oggetto; }
    public String getCorpo() { return corpo; }
    public String getData() { return data; }

    @Override
    public String toString() {
        return id + "," + mittente + "," + destinatari + "," + oggetto + "," + corpo + "," + data;
    }
}