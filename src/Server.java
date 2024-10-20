import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8080;
    private static final String STORAGE_PATH = "storage/";

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started, listening on port " + PORT);

        // Create storage directory if it doesn't exist
        File storageDir = new File(STORAGE_PATH);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            executor.execute(new ClientHandler(clientSocket));
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                String command = in.readUTF();
                if (command.equals("UPLOAD")) {
                    receiveFile(in, out);
                } else if (command.equals("DOWNLOAD")) {
                    sendFile(in, out);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void receiveFile(DataInputStream in, DataOutputStream out) throws IOException {
            String fileName = in.readUTF();
            long fileSize = in.readLong();

            FileOutputStream fos = new FileOutputStream(Server.STORAGE_PATH + fileName);
            byte[] buffer = new byte[4096];
            long read = 0;
            while (read < fileSize) {
                int bytesRead = in.read(buffer);
                fos.write(buffer, 0, bytesRead);
                read += bytesRead;
            }
            fos.close();
            out.writeUTF("File uploaded successfully.");
        }

        private void sendFile(DataInputStream in, DataOutputStream out) throws IOException {
            String fileName = in.readUTF();
            File file = new File(Server.STORAGE_PATH + fileName);

            if (!file.exists()) {
                out.writeUTF("File not found.");
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            out.writeUTF("File found.");
            out.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            fis.close();
        }
    }
}
