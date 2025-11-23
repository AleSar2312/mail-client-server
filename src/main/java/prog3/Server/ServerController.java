package prog3.Server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.application.Platform;

public class ServerController {

    @FXML private TextArea logArea;
    @FXML private Label lblStatus;
    @FXML private Label lblConnections;

    private int connectionCount = 0;
    private ServerModel serverModel;
    private Log log;

    @FXML
    private void initialize() {
        log = new Log();
        serverModel = new ServerModel(this);
        serverModel.start();
        addLog("Server avviato");
        lblConnections.setText("0");
    }

    public void addLog(String message) {
        Platform.runLater(() -> {
            log.addEntry(message);
            logArea.appendText(log.getLastEntry() + "\n");
        });
    }

    public void incrementConnections() {
        Platform.runLater(() -> {
            connectionCount++;
            lblConnections.setText(String.valueOf(connectionCount));
        });
    }

    public void decrementConnections() {
        Platform.runLater(() -> {
            connectionCount--;
            lblConnections.setText(String.valueOf(connectionCount));
        });
    }
}