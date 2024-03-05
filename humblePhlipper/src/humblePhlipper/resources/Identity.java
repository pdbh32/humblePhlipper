package humblePhlipper.resources;

import java.util.*;

public class Identity {
    public UUID uuid;
    public Map<String, List<String>> requestHeaders;
    public Identity() {
    }
    public int deterministicInt(int bound) {
        long uuidLong = Math.abs(uuid.getMostSignificantBits());
        return (int)(uuidLong % bound);
    }
}