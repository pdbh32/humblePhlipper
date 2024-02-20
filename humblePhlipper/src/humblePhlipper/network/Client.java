package humblePhlipper.network;

import org.dreambot.api.utilities.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Random;

public class Client {
    public Client(int port, Protocol cp, Protocol sp) {
        try (
                Socket socket = new Socket("localhost", port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            cp.feed(in, out);
            cp.go();
        } catch (IOException e) {
            new Server(port, sp);
        }
    }
}
//
// Explicit timeout with randomisation could be the solution to clients failing to find a server
// and more than one server being instantiated (due to clients being instantiated at almost exactly
// the same time) - requires more testing.
//
/*public class Client {
    public Client(int port, Protocol cp, Protocol sp) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), new Random().nextInt(501) + 2500);
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                cp.feed(in, out);
                cp.go();
            }
        } catch (IOException e) {
            new Server(port, sp);
        }
    }
}*/