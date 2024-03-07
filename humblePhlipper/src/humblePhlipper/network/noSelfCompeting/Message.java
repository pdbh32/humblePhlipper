package humblePhlipper.network.noSelfCompeting;

import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Message {
    private humblePhlipper.ResourceManager rm;
    public Set<Integer> tradedIds = new HashSet<>();
    public Message(humblePhlipper.ResourceManager rm) {
        this.rm = rm;
        try {
            for (int i = 0; i < 8; i++) {
                humblePhlipper.dbGE.Slot geSlot = humblePhlipper.dbGE.Slot.get(i);
                if ("Empty".equals(geSlot.getType())) {
                    continue;
                }
                tradedIds.add(geSlot.getItemId());
            }
            for (Map.Entry<Integer, humblePhlipper.resources.Items.Item> entry : rm.items.entrySet()) {
                if (entry.getValue().getBought() <= entry.getValue().getSold()) {
                    continue;
                }
                tradedIds.add(entry.getKey());
            }
        } catch (Exception ignored) {}

    }
    public Message(humblePhlipper.ResourceManager rm, Set<Integer> tradedIds) {
        this.rm = rm;
        this.tradedIds = tradedIds;
    }
    public Message(humblePhlipper.ResourceManager rm, String message) {
        this.rm = rm;
        tradedIds = rm.gson.fromJson(message, new TypeToken<Set<Integer>>() {}.getType());
    }

    public String CSV() {
        return rm.gson.toJson(tradedIds);
    }
}
