package phinancialMule.network;

public class ServerProtocol {
    public ClientMessage read(String message) {
        return new ClientMessage(message);
    }
    public ServerMessage respond(ClientMessage cm) {
        if (cm.status == phinancialMule.enums.Status.DISTRIBUTING || (cm.status == phinancialMule.enums.Status.RECEIVING && phinancialMule.Main.status == phinancialMule.enums.Status.DISTRIBUTING)) {
            return new ServerMessage(phinancialMule.Main.myName, phinancialMule.enums.Action.TRADE);
        }
        return new ServerMessage(phinancialMule.Main.myName, phinancialMule.enums.Action.IDLE);
    }
}