package prog3.Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.File;
import java.io.FileWriter;

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

    private boolean saveEmail(String to, String from, String subject, String body) {
        if (!mailBoxes.userExists(to)) {
            return false;
        }

        try {
            File inboxFile = new File("src/main/java/prog3/Server/mailboxes/" + to + "/inbox.txt");

            // Crea directory se non esiste
            inboxFile.getParentFile().mkdirs();

            // Genera ID email (timestamp)
            int emailId = (int) System.currentTimeMillis();

            // Formato: id;from;to;subject;body;date
            String emailLine = emailId + ";" + from + ";" + to + ";" + subject + ";" + body + ";" + new java.util.Date();

            // Append al file
            try (PrintWriter writer = new PrintWriter(new FileWriter(inboxFile, true))) {
                writer.println(emailLine);
            }

            return true;

        } catch (IOException e) {
            controller.addLog("Errore salvataggio email: " + e.getMessage());
            return false;
        }
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

            // Leggi UN SOLO comando e rispondi
            String message = in.readLine();
            if (message != null) {
                controller.addLog("Ricevuto: " + message);

                String[] parts = message.split(":", 5);
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
                else if (command.equals("SEND_EMAIL") && parts.length == 5) {
                    String from = parts[1];
                    String to = parts[2];
                    String subject = parts[3];
                    String body = parts[4];

                    boolean saved = saveEmail(to, from, subject, body);

                    if (saved) {
                        out.println("OK:Email inviata");
                        controller.addLog("Email salvata da " + from + " a " + to);
                    } else {
                        out.println("ERROR:Destinatario non trovato");
                        controller.addLog("Errore: destinatario " + to + " non esiste");
                    }
                }
                else if (command.equals("DELETE_EMAIL") && parts.length == 3) {
                    String userEmail = parts[1];
                    String emailId = parts[2];

                    controller.addLog("Eliminata email ID " + emailId + " da " + userEmail);
                    out.println("OK:Email eliminata");
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