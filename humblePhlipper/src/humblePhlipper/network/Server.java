package humblePhlipper.network;

import org.dreambot.api.utilities.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

public class Server {
    public Server(int port, Protocol sp) {
        Instant startTime = Instant.now();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            sp.begin();
            serverSocket.setSoTimeout(3000); // Maximum time serverSocket will block whilst waiting for a client connection via accept()
            while (Duration.between(startTime, Instant.now()).toMillis() < 3000) { // Time the while loop will run to accept() clients
                new ServerThread(serverSocket.accept(), sp).start();
            }
        } catch (SocketTimeoutException ignored) {
            System.err.println(LocalDateTime.now() + " SERVER-side SocketTimeoutException");
        } catch (IOException e) {
            System.err.println("IOException for port " + port);
        }
        sp.finish();
    }
}