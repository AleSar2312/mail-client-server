package prog3.Server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.application.Platform;

public class ServerController {

    @FXML
    private ListView<String> logList;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblConnections;

    private int connectionCount = 0;  // IMPORTANTE: campo di istanza
    private ServerModel serverModel;
    private Log log;

    @FXML
    private void initialize() {
        log = new Log();

        if (logList != null) {
            logList.setItems(log.getLogList());
        }

        serverModel = new ServerModel(this, log);
        serverModel.start();

        if (lblConnections != null) {
            lblConnections.setText("0");
        }
        if (lblStatus != null) {
            lblStatus.setText("Server attivo");
        }
    }

    public synchronized void incrementConnections() {
        Platform.runLater(() -> {
            connectionCount++;
            System.out.println("DEBUG: Incremento connessioni. Totale: " + connectionCount);
            if (lblConnections != null) {
                lblConnections.setText(String.valueOf(connectionCount));
            }
        });
    }

    public synchronized void decrementConnections() {
        Platform.runLater(() -> {
            connectionCount--;
            System.out.println("DEBUG: Decremento connessioni. Totale: " + connectionCount);
            if (lblConnections != null) {
                lblConnections.setText(String.valueOf(connectionCount));
            }
        });
    }
}