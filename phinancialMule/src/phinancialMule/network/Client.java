package phinancialMule.network;

import java.io.*;
import java.net.*;

public class Client {
    public ClientProtocol cp = new ClientProtocol();

    public Client() {
        try (
                Socket socket = new Socket("localhost", 1776);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            ClientMessage cm = cp.write();
            out.println(cm.CSV());
            ServerMessage sm = cp.read(in.readLine());
            if (phinancialMule.Main.action == phinancialMule.enums.Action.IDLE && sm.action == phinancialMule.enums.Action.TRADE) {
                phinancialMule.Main.action = phinancialMule.enums.Action.TRADE;
                phinancialMule.Main.muling.theirName = sm.muleName;
            }
        } catch (UnknownHostException e) {
            System.err.println("UnknownHostException for localhost");
            phinancialMule.Main.status = phinancialMule.enums.Status.ERROR;
        } catch (IOException e) {
            System.err.println("IOException for localhost");
            phinancialMule.Main.status = phinancialMule.enums.Status.ERROR;
        }
    }
}