import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter command (UPLOAD/DOWNLOAD): ");
            String command = console.readLine();
            out.writeUTF(command);

            if (command.equals("UPLOAD")) {
                uploadFile(out, in, console);
            } else if (command.equals("DOWNLOAD")) {
                downloadFile(out, in, console);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void uploadFile(DataOutputStream out, DataInputStream in, BufferedReader console) throws IOException {
        System.out.println("Enter the file path to upload: ");
        String filePath = console.readLine();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found.");
            return;
        }

        out.writeUTF(file.getName());
        out.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fis.close();

        System.out.println(in.readUTF());
    }

    private static void downloadFile(DataOutputStream out, DataInputStream in, BufferedReader console) throws IOException {
        System.out.println("Enter the file name to download: ");
        String fileName = console.readLine();
        out.writeUTF(fileName);

        String response = in.readUTF();
        if (response.equals("File not found.")) {
            System.out.println(response);
            return;
        }

        long fileSize = in.readLong();
        FileOutputStream fos = new FileOutputStream("downloaded_" + fileName);
        byte[] buffer = new byte[4096];
        long read = 0;
        while (read < fileSize) {
            int bytesRead = in.read(buffer);
            fos.write(buffer, 0, bytesRead);
            read += bytesRead;
        }
        fos.close();

        System.out.println("File downloaded successfully.");
    }
}
