package humblePhlipper.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private Protocol sp;

    public ServerThread(Socket socket, Protocol sp) {
        this.socket = socket;
        this.sp = sp;
    }
    public void run() {
        try (
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            sp.go(in, out);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}