package humblePhlipper.network;

import org.dreambot.api.utilities.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;

public class Server {
    public Server(int port, Protocol sp) {
        if (humblePhlipper.Main.rm.config.getDebug()) { Logger.log("<SERVER>"); }
        Instant startTime = Instant.now();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            sp.begin();
            serverSocket.setSoTimeout(3000); // Maximum time serverSocket will block whilst waiting for a client connection via accept()
            while (Duration.between(startTime, Instant.now()).toMillis() < 3000) { // Time the while loop will run to accept() clients
                new ServerThread(serverSocket.accept(), sp).start();
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            System.err.println("IOException for port " + port);
        }
        sp.finish();
        if (humblePhlipper.Main.rm.config.getDebug()) { Logger.log("</SERVER>"); }
    }
}