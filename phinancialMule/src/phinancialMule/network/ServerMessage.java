package phinancialMule.network;

import phinancialMule.enums.Action;

public class ServerMessage {
    String muleName;
    Action action;
    public ServerMessage(String muleName, Action action) {
        this.muleName = muleName;
        this.action = action;
    }
    public ServerMessage(String message) {
        String[] components = message.split(",");
        this.muleName = components[0];
        this.action = Action.valueOf(components[1]);
    }

    public String CSV() {
        return this.muleName + "," + this.action;
    }

}