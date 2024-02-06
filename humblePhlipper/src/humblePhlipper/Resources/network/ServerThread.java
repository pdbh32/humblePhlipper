package humblePhlipper.resources.network;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    private Socket socket;
    private ServerProtocol sp;

    public ServerThread(Socket socket, ServerProtocol sp) {
        this.socket = socket;
        this.sp = sp;
    }
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            ClientMessage cm = sp.read(in.readLine());
            sp.addData(cm);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}