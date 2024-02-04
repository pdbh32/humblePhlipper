package phinancialMule;

import java.awt.*;

public class Paint {
    public void onPaint(Graphics g) {
        if (phinancialMule.Main.role == phinancialMule.enums.Role.MULE) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect(350,10,160,110);
        g.setColor(Color.WHITE);
        g.fillRect(360,20,140,90);
        g.setColor(Color.BLACK);
        g.drawString("Role: " + phinancialMule.Main.role, 365,40);
        g.drawString("Status: " + phinancialMule.Main.status,365,60);
        g.drawString("Action: " + phinancialMule.Main.action, 365,80);
        g.drawString("Total GP: " + phinancialMule.Muling.totalGp,365,100);
    }
}