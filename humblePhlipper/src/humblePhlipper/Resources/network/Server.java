package humblePhlipper.resources.network;

import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;

public class Server {
    public Server() {
        ServerProtocol sp = new ServerProtocol();
        Instant startTime = Instant.now();
        try (ServerSocket serverSocket = new ServerSocket(1969)) {
            serverSocket.setSoTimeout(30000);
            while (Duration.between(startTime, Instant.now()).toMillis() < 30000) {
                new ServerThread(serverSocket.accept(), sp).start();
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            System.err.println("IOException for port 1969, try restarting your client");
        }
        sp.sendWebhook();
    }
}