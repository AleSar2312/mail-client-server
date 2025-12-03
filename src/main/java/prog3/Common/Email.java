package prog3.Common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Email {
    private SimpleStringProperty mittente = new SimpleStringProperty();
    private SimpleStringProperty destinatari = new SimpleStringProperty();
    private SimpleStringProperty oggetto = new SimpleStringProperty();
    private SimpleStringProperty corpo = new SimpleStringProperty();
    private SimpleStringProperty data = new SimpleStringProperty();
    private int id;

    public Email(int id, String mittente, String destinatari, String oggetto, String corpo, String data) {
        this.id = id;
        this.mittente.setValue(mittente);
        this.destinatari.setValue(destinatari);
        this.oggetto.setValue(oggetto);
        this.corpo.setValue(corpo);
        this.data.setValue(data);
    }

    public int getId() { return id; }

    public String getMittente() { return mittente.get(); }
    public StringProperty mittenteProperty() { return mittente; }

    public String getDestinatari() { return destinatari.get(); }
    public StringProperty destinatariProperty() { return destinatari; }

    public String getOggetto() { return oggetto.get(); }
    public StringProperty oggettoProperty() { return oggetto; }

    public String getCorpo() { return corpo.get(); }
    public StringProperty corpoProperty() { return corpo; }

    public String getData() { return data.get(); }
    public StringProperty dataProperty() { return data; }

    @Override
    public String toString() {
        return "Da: " + getMittente() + "\nData: " + getData() + "\nOggetto: " + getOggetto();
    }
}