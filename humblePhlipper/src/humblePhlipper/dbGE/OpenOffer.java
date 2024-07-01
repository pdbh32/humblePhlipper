package humblePhlipper.dbGE;

import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenOffer {
    private static final int SLEEP  = humblePhlipper.Main.SLEEP;
    public static boolean isBuyOffer() {
        return Widgets.get(465, 15, 4).getText().equals("Buy offer");
    }
    public static Integer getTransferredAmount() {
        Pattern pattern = Pattern.compile("<col=ffb83f>([,\\d]+)</col>");
        Matcher matcher = pattern.matcher(Widgets.get(465, 23, 1).getText());
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1).replaceAll(",", ""));
    }

    // Post tax
    public static Integer getTransferredValue() {
        Pattern pattern = Pattern.compile("<col=ffb83f>([,\\d]+)</col>");
        Matcher matcher = pattern.matcher(Widgets.get(465, 23, 1).getText());
        if (!matcher.find()) {
            return null;
        }
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1).replaceAll(",", ""));
        } catch (IllegalStateException e) {
            return 1;
        }
    }

    public static Boolean goBack() {
        if (!GrandExchange.isBuyOpen() & !GrandExchange.isSellOpen()) { return null; }
        if (Sleep.sleepUntil(GrandExchange::goBack, SLEEP)) {
            Sleep.sleep(SLEEP);
            return true;
        }
        return false;
    }

    public static Boolean closeConvenienceFee() {
        WidgetChild downArrow = Widgets.get(289, 9, 5);
        if (downArrow == null) { return null; }
        if (!downArrow.isVisible()) { return null; }
        if (Sleep.sleepUntil(downArrow::interact, SLEEP)) {
            Sleep.sleep(SLEEP);
        } else {
            return false;
        }
        if (Sleep.sleepUntil(downArrow::interact, SLEEP)) {
            Sleep.sleep(SLEEP);
        } else {
            return false;
        }
        if (Sleep.sleepUntil(downArrow::interact, SLEEP)) {
            Sleep.sleep(SLEEP);
        } else {
            return false;
        }
        WidgetChild closeButton = Widgets.get(289, 7, 8);
        if (closeButton == null) { return false; }
        if (!closeButton.isVisible()) { return false; }
        if (Sleep.sleepUntil(closeButton::interact, SLEEP)) {
            Sleep.sleep(SLEEP);
            return true;
        }
        return false;
    }
}
