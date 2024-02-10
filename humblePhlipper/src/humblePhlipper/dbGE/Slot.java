package humblePhlipper.dbGE;

import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;

// GrandExchangeItem is very buggy, instead use GrandExchange widgets and iterate through slots.
public class Slot {
    public static final int maxTradeBarWidth = 105;

    private final int slotIndex;

    private Slot(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public static Slot get(int slotIndex) {
        return new Slot(slotIndex);
    }

    private WidgetChild getSlotWidget() {
        return Widgets.get(465, 7 + slotIndex);
    }

    public String getType() {
        return getSlotWidget().getChild(16).getText(); // "Sell", "Buy", "Empty"
    }

    public boolean isBuyOffer() {
        return getType().equals("Buy");
    }

    public boolean isSellOffer() {
        return getType().equals("Sell");
    }

    public int getTradeBarWidth() {
        return getSlotWidget().getChild(22).getWidth(); // 0 to 105
    }

    public boolean isReadyToCollect() {
        return getTradeBarWidth() == maxTradeBarWidth;
    }

    public int getItemId() {
        return getSlotWidget().getChild(18).getItemId(); // -1 if "Empty"
    }

    public int getItemStack() {
        return getSlotWidget().getChild(18).getItemStack(); // 0 if "Empty"
    }
    public int getPrice() {
        if (getSlotWidget().getChild(25).getText().isEmpty()) {
            return -1;
        }
        String text = getSlotWidget().getChild(25).getText();
        text = text.replaceAll("[^0-9]","");
        return Integer.parseInt(text);
    }

    public static int getFirstOpenSlot() {
        for (int i = 0; i < 8; i++) {
            if (get(i).getType().equals("Empty")) {
                return i;
            }
        }
        return -1;
    }

    public static boolean openSlotInterface(int i) {
        return get(i).getSlotWidget().getChild(2).interact();
    }
}
