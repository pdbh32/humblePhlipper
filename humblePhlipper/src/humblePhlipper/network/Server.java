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
            serverSocket.setSoTimeout(1000);
            sp.begin();
            while (Duration.between(startTime, Instant.now()).toMillis() < 1000) {
                new ServerThread(serverSocket.accept(), sp).start();
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            System.err.println("IOException for port " + port);
        }
        sp.finish();
    }
}