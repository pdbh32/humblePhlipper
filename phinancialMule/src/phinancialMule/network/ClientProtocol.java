package phinancialMule.network;

public class ClientProtocol {
    public ServerMessage read(String message) {
        return new ServerMessage(message);
    }
    public ClientMessage write() {
        return new ClientMessage(phinancialMule.Main.myName, phinancialMule.Main.status);
    }
}