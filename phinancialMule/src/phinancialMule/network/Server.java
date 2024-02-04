package phinancialMule.network;

import java.net.*;
import java.io.*;

public class Server implements Runnable {
    public ServerProtocol sp = new ServerProtocol();

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1776)) {
            while (phinancialMule.Main.status != phinancialMule.enums.Status.FINISHED) {
                Socket socket = serverSocket.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ClientMessage cm = sp.read(in.readLine());
                ServerMessage sm = sp.respond(cm);
                out.println(sm.CSV());
                if (phinancialMule.Main.action == phinancialMule.enums.Action.IDLE && sm.action == phinancialMule.enums.Action.TRADE) {
                    phinancialMule.Main.action = phinancialMule.enums.Action.TRADE;
                    phinancialMule.Main.muling.theirName = cm.slaveName;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException for port 1776, try restarting your client");
            phinancialMule.Main.status = phinancialMule.enums.Status.ERROR;
        }
    }
}