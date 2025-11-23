package prog3.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllMailBoxes {

    private List<String> mailboxes;
    private static final String MAILBOXES_PATH = "src/main/java/prog3/Server/mailboxes/";

    public AllMailBoxes() {
        this.mailboxes = new ArrayList<>();
        loadMailboxes();
    }

    private void loadMailboxes() {
        File dir = new File(MAILBOXES_PATH);
        if (dir.exists() && dir.isDirectory()) {
            File[] folders = dir.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    mailboxes.add(folder.getName());
                }
            }
        }
    }

    public boolean userExists(String email) {
        return mailboxes.contains(email);
    }

    public List<String> getAllMailboxes() {
        return mailboxes;
    }
}
