//Config.java

import java.util.HashMap;
import java.util.Map;

public class Config {
    private String[] params;

    public Config() {
    }
    public Config(String... params) {
        this.params = params;
    }

    public String[] getParams() {
        return params;
    }
    public void setParams(String[] params) {
        this.params = params;
    }

    public void setConfig() {
        float timeout = Float.POSITIVE_INFINITY;
        boolean sysExit = false;
        float maxBidVol = 100;
        int profitCutoff = Integer.MAX_VALUE;
        Map<Integer, Integer> idTargetMap = new HashMap<>();

        if (this.params != null) {
            for (String param : this.params) {
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
                            case "profitCutoff":
                                try {
                                    profitCutoff = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    // timeout left at default
                                }
                                break;
                        }
                    }
                }

                if (param.startsWith("{") && param.endsWith("}")) {
                    String[] itemVol = param.substring(1, param.length() - 1).split(":");
                    if (itemVol.length == 2) {
                        try {
                            int ID = Main.api.getIdFromString(itemVol[0].trim()); // accept both item names and IDs
                            if (ID != -1) { // valid name / id
                                int targetVol = (Integer.parseInt(itemVol[1].trim()) <= 0) ? Main.api.mappingMap.get(ID).getLimit() : Integer.parseInt(itemVol[1].trim()); // default to 4hr GE limit for -1
                                if (idTargetMap.keySet().stream().allMatch(id -> id != ID)) { // avoid duplicates
                                    idTargetMap.put(ID, targetVol);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // item not added to itemList
                        }
                    }
                }
            }
        }
        if (idTargetMap.isEmpty()) {
            idTargetMap.put(1511, 15000); // Logs
            idTargetMap.put(560, 25000); // Death rune
        }
        Map<Integer, Item> itemMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : idTargetMap.entrySet()) {
            int ID = entry.getKey();
            int target = entry.getValue();
            itemMap.put(ID, new Item(ID, target));
        }
        Main.setTimeout(timeout);
        Main.setSysExit(sysExit);
        Main.setMaxBidVol(maxBidVol);
        Main.setProfitCutoff(profitCutoff);
        Main.setItemMap(itemMap);
    }
}








