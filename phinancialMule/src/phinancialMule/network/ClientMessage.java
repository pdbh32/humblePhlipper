package phinancialMule.network;

import phinancialMule.enums.Status;

public class ClientMessage {
    String slaveName;
    Status status;
    public ClientMessage(String slaveName, Status status) {
        this.slaveName = slaveName;
        this.status = status;
    }
    public ClientMessage(String message) {
        String[] components = message.split(",");
        this.slaveName = components[0];
        this.status = Status.valueOf(components[1]);
    }
    public String CSV() {
        return this.slaveName + "," + this.status;
    }
}