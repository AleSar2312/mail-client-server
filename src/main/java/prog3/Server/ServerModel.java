package prog3.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerModel implements Runnable {

    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executorService;
    private ServerController controller;
    private AllMailBoxes mailBoxes;
    private Log log;

    public ServerModel(ServerController controller, Log log) {
        this.controller = controller;
        this.log = log;
        this.mailBoxes = new AllMailBoxes();
        this.running = false;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public void start() {
        running = true;
        Thread serverThread = new Thread(this);
        serverThread.start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            log.addEntry("Server in ascolto sulla porta " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                log.addEntry("Nuova connessione da: " + clientSocket.getInetAddress());

                // NON incrementare qui - lo fa ClientHandler solo al login iniziale
                executorService.execute(new ClientHandler(clientSocket, this));
            }
        } catch (IOException e) {
            if (running) {
                log.addEntry("Errore server: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            log.addEntry("Server fermato");
        } catch (IOException e) {
            log.addEntry("Errore durante lo stop: " + e.getMessage());
        }
    }

    public Log getLog() {
        return log;
    }

    public ServerController getController() {
        return controller;
    }

    public AllMailBoxes getMailBoxes() {
        return mailBoxes;
    }
}