package prog3.Server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Log {

    private List<String> logEntries;
    private DateTimeFormatter formatter;

    public Log() {
        this.logEntries = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    public void addEntry(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String entry = "[" + timestamp + "] " + message;
        logEntries.add(entry);
    }

    public List<String> getEntries() {
        return logEntries;
    }

    public String getLastEntry() {
        if (logEntries.isEmpty()) {
            return "";
        }
        return logEntries.get(logEntries.size() - 1);
    }
}