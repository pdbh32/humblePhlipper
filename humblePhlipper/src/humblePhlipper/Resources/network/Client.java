package humblePhlipper.resources.network;

import java.io.*;
import java.net.*;

public class Client {
    public Client() {
        try (
                Socket socket = new Socket("localhost", 1969);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            out.println(new ClientMessage().CSV());
        } catch (IOException e) {
            new Server();
        }
    }
}