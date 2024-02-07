package phinancialMule;

import org.dreambot.api.Client;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import javax.swing.*;
import java.awt.*;

@ScriptManifest(category = Category.MISC, name = "phinancialMule", author = "apnasus", version = 1.01)
public class Main extends AbstractScript {
    private static boolean running = false;
    public static String myName;
    private static GUI gui;
    private static final Paint paint = new Paint();
    public static Muling muling = new Muling();
    public static phinancialMule.enums.Role role; // enum
    public static phinancialMule.enums.Status status; // enum
    public static phinancialMule.enums.Action action = phinancialMule.enums.Action.IDLE; // enum
    public static Integer numSlaves; // config param
    public static Integer distributionAmount; // config param

    public static boolean init(java.lang.String... params) {
        if (params.length > 2) {
            return false;
        }
        role = phinancialMule.enums.Role.MULE;
        status = phinancialMule.enums.Status.RECEIVING;
        Thread serverThread = new Thread(new phinancialMule.network.Server());
        switch (params.length) {
            case 0:
                role = phinancialMule.enums.Role.SLAVE;
                status = phinancialMule.enums.Status.DISTRIBUTING;
                serverThread = null;
                break;
            case 1:
                numSlaves = Integer.parseInt(params[0]);
                break;
            case 2:
                numSlaves = Integer.parseInt(params[0]);
                distributionAmount = Integer.parseInt(params[1]);
                break;
        }
        if (serverThread != null) {
            serverThread.start();
        }
        if (Client.isLoggedIn()) {
            setMyName();
        }
        running = true;
        return true;
    }

    private static void setMyName() {
        String widgetText = Widgets.get(162, 55).getText();
        myName = widgetText.substring(0, widgetText.indexOf(":"));
    }

    @Override
    public void onSolverEnd(RandomSolver randomSolver) {
        setMyName();
    }
    @Override
    public void onStart(java.lang.String... params) {
        if (!init(params)) {
            stop();
        }
    }
    @Override
    public void onStart() {
        SwingUtilities.invokeLater(() -> {
            gui = new GUI();
        });
    }

    @Override
    public void onPaint(Graphics g) {
        if (running) {
            paint.onPaint(g);
        }
    }

    @Override
    public int onLoop() {
        if (status == phinancialMule.enums.Status.FINISHED) {
            return -1;
        }
        if (!Client.isLoggedIn()) {
            return 1000;
        }
        if (!running) {
            return 1000;
        }
        if (role == phinancialMule.enums.Role.SLAVE) {
            new phinancialMule.network.Client();
        }
        if (action == phinancialMule.enums.Action.TRADE) {
            muling.Trade();
        }
        return 1000;
    }

    @Override
    public void onExit() {
        status = phinancialMule.enums.Status.FINISHED;
        if (gui != null) {
            gui.dispose();
        }
    }
}