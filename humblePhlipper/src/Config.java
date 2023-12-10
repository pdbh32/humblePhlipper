import java.util.*;
public class Config {

    public float timeout;
    public boolean sysExit;
    public float maxBidVol;
    public List<Item> itemList;

    public Config() {
        defaultTimeout();
        defaultSysExit();
        defaultMaxBidVol();
        defaultItemList();
    }
    private void defaultTimeout() {
        timeout = Float.POSITIVE_INFINITY;
    }

    private void defaultSysExit() {
        sysExit = false;
    }

    private void defaultMaxBidVol() { maxBidVol = 100; }
    private void defaultItemList() {
        itemList = new ArrayList<>();
        itemList.add(new Item("Logs",1511,15000));
        itemList.add(new Item("Death rune",560,25000));
    }
    public void setParams(String... params) {
        defaultTimeout();
        defaultSysExit();
        itemList = new ArrayList<>();

        for (String param : params) {
            if (param.startsWith("[") && param.endsWith("]")) {
                String[] optionValue = param.substring(1, param.length() - 1).split(":");
                if (optionValue.length == 2) {
                    String option = optionValue[0].trim();
                    String value = optionValue[1].trim();
                    switch (option) {
                        case "timeout":
                            try {
                                timeout = Float.parseFloat(value);
                            } catch (NumberFormatException e) {
                                // timeout left at default
                            }
                            break;
                        case "sysExit":
                            try {
                                sysExit = Boolean.parseBoolean(value);
                            } catch (IllegalArgumentException e) {
                                // sysExit left as default
                            }
                            break;
                        case "maxBidVol":
                            try {
                                float percentage = Float.parseFloat(value);
                                if (percentage <= 100 && percentage >= 0) {
                                    maxBidVol = percentage;
                                }
                            } catch (NumberFormatException e) {
                                // maxBidVol left as default
                            }
                            break;
                    }
                }
            }

            if (param.startsWith("{") && param.endsWith("}")) {
                String[] itemVol = param.substring(1, param.length() - 1).split(":");
                if (itemVol.length == 2) {
                    try {
                        int ID = Main.api.getIdFromString(itemVol[0].trim());
                        if (ID != -1) {
                            int targetVol = (Integer.parseInt(itemVol[1].trim()) <= 0) ? Main.api.mappingMap.get(ID).getLimit() : Integer.parseInt(itemVol[1].trim());
                            API.Mapping mapping = Main.api.mappingMap.get(ID);
                            if (itemList.stream().allMatch(item -> item.id != ID)) {
                                itemList.add(new Item(Main.api.mappingMap.get(ID).getName(), ID, targetVol));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // item not added to itemList
                    }
                }
            }
        }
        if (itemList.isEmpty()) {
            defaultItemList();
        }
    }
}
