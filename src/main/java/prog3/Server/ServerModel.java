package prog3.Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerModel {

    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean running;
    private CopyOnWriteArrayList<Socket> clients;
    private ServerController controller;
    private AllMailBoxes mailBoxes;

    public ServerModel(ServerController controller) {
        this.controller = controller;
        this.clients = new CopyOnWriteArrayList<>();
        this.mailBoxes = new AllMailBoxes();
        this.running = false;
    }

    public void start() {
        running = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                controller.addLog("Server in ascolto sulla porta " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    clients.add(clientSocket);
                    controller.addLog("Nuova connessione da: " + clientSocket.getInetAddress());
                    controller.incrementConnections();

                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                if (running) {
                    controller.addLog("Errore server: " + e.getMessage());
                }
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                controller.addLog("Ricevuto: " + message);

                String[] parts = message.split(":", 2);
                String command = parts[0];

                if (command.equals("LOGIN") && parts.length > 1) {
                    String email = parts[1];
                    if (mailBoxes.userExists(email)) {
                        out.println("OK:Login effettuato");
                        controller.addLog("Login riuscito per: " + email);
                    } else {
                        out.println("ERROR:Utente non trovato");
                        controller.addLog("Login fallito per: " + email);
                    }
                }
            }

        } catch (IOException e) {
            controller.addLog("Errore comunicazione client: " + e.getMessage());
        } finally {
            closeClient(socket);
        }
    }

    private void closeClient(Socket socket) {
        try {
            clients.remove(socket);
            socket.close();
            controller.decrementConnections();
            controller.addLog("Client disconnesso");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (Socket client : clients) {
                client.close();
            }
            controller.addLog("Server fermato");
        } catch (IOException e) {
            controller.addLog("Errore durante lo stop: " + e.getMessage());
        }
    }
}