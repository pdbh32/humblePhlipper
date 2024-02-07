package humblePhlipper.dbGE;

import org.dreambot.api.methods.widget.Widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenOffer {
    public static boolean isBuyOffer() {
        return Widgets.get(465, 15, 4).getText().equals("Buy offer");
    }
    public static int getTransferredAmount() {
        Pattern pattern = Pattern.compile("<col=ffb83f>([,\\d]+)</col>");
        Matcher matcher = pattern.matcher(Widgets.get(465, 23, 1).getText());
        matcher.find();
        return Integer.parseInt(matcher.group(1).replaceAll(",", ""));
    }

    // Post tax
    public static int getTransferredValue() {
        Pattern pattern = Pattern.compile("<col=ffb83f>([,\\d]+)</col>");
        Matcher matcher = pattern.matcher(Widgets.get(465, 23, 1).getText());
        matcher.find();
        matcher.find();
        return Integer.parseInt(matcher.group(1).replaceAll(",", ""));
    }
}
