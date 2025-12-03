package prog3.Server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private ObservableList<String> logList;
    private DateTimeFormatter formatter;

    public Log() {
        this.logList = FXCollections.observableArrayList();
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    public void addEntry(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(formatter);
            String entry = "[" + timestamp + "] " + message;
            logList.add(entry);
        });
    }

    public ObservableList<String> getLogList() {
        return logList;
    }
}