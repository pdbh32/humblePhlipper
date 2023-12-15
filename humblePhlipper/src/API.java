//API.java

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.dreambot.api.utilities.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class API {
    public Map<Integer, Latest.Item> latestMap;
    public Map<Integer, Mapping> mappingMap;

    public API() {
        updateLatest();
        updateMapping();
    }

    public void updateLatest() {
        String jsonString = GetJSON("https://prices.runescape.wiki/api/v1/osrs/latest");
        latestMap = parseLatestJson(jsonString);
    }

    public void updateMapping() {
        String jsonString = GetJSON("https://prices.runescape.wiki/api/v1/osrs/mapping");
        mappingMap = parseMappingJson(jsonString);
    }

    private Map<Integer, Latest.Item> parseLatestJson(String jsonString) {
        Map<Integer, Latest.Item> latestMap = new HashMap<>();

        try {
            // Parse JSON manually for the latest API

            // Extract data field
            String dataField = jsonString.substring(jsonString.indexOf("\"data\":") + 7);
            dataField = dataField.substring(0, dataField.length() - 1);

            // Parse the "data" JSON object
            JsonObject dataObject = new JsonParser().parse(dataField).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : dataObject.entrySet()) {
                int itemId = Integer.parseInt(entry.getKey());

                // Parse the nested JSON object for high/low values
                JsonElement highElement = entry.getValue().getAsJsonObject().get("high");
                JsonElement lowElement = entry.getValue().getAsJsonObject().get("low");

                // Check for null values before extracting
                int high = (highElement instanceof JsonNull) ? 0 : highElement.getAsInt();
                int low = (lowElement instanceof JsonNull) ? 0 : lowElement.getAsInt();

                // Create Latest.Item and put it into the map
                Latest.Item item = new Latest.Item();
                item.setHigh(high);
                item.setLow(low);
                latestMap.put(itemId, item);
            }
        } catch (Exception e) {
            Logger.log(e);
        }

        return latestMap;
    }

    private Map<Integer, Mapping> parseMappingJson(String jsonString) {
        Map<Integer, Mapping> mappingMap = new HashMap<>();

        try {
            // Convert the JSON array to an array of JSON objects
            jsonString = jsonString.substring(1, jsonString.length() - 1);
            String[] entries = jsonString.split("\\},\\{");

            for (String entry : entries) {
                // Create a Mapping object and put it into the map
                Mapping mapping = new Mapping();

                // Split the entry into key-value pairs
                String[] keyValuePairs = entry.split(",");

                for (String pair : keyValuePairs) {
                    // Split each key-value pair by ":"
                    String[] keyValue = pair.split(":");

                    if (keyValue.length == 2) { // Check if the pair has both key and value
                        String key = keyValue[0].replaceAll("[{}\"]", "").trim();
                        String value = keyValue[1].replaceAll("[{}\"]", "").trim();

                        // Set the corresponding field in the Mapping object
                        switch (key) {
                            case "examine":
                                mapping.setExamine(value);
                                break;
                            case "id":
                                mapping.setId(Integer.parseInt(value));
                                break;
                            case "members":
                                mapping.setMembers(Boolean.parseBoolean(value));
                                break;
                            case "lowalch":
                                mapping.setLowalch(Integer.parseInt(value));
                                break;
                            case "limit":
                                mapping.setLimit(Integer.parseInt(value));
                                break;
                            case "value":
                                mapping.setValue(Integer.parseInt(value));
                                break;
                            case "highalch":
                                mapping.setHighalch(Integer.parseInt(value));
                                break;
                            case "icon":
                                mapping.setIcon(value);
                                break;
                            case "name":
                                mapping.setName(value);
                                break;
                        }
                    }
                }

                mappingMap.put(mapping.getId(), mapping);
            }
        } catch (Exception e) {
            Logger.log(e);
        }

        return mappingMap;
    }


    private static String GetJSON(String url) {
        try {
            // Set the API
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // Set the request method
            connection.setRequestMethod("GET");

            // Set the header
            connection.setRequestProperty("User-Agent", "f2p_flipper");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response from the API
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response manually as a string
                String jsonString = response.toString();
                return jsonString;
            } else {
                Logger.log("Error: HTTP request failed with response code " + responseCode);
            }
        } catch (IOException e) {
            Logger.log(e);
        }
        return null;
    }

    public static class Latest {
        private Map<String, Item> data;

        public Map<String, Item> getData() {
            return data;
        }

        public void setData(Map<String, Item> data) {
            this.data = data;
        }

        public static class Item extends Latest {
            private int high;
            private long highTime;
            private int low;
            private long lowTime;

            public int getHigh() {
                return high;
            }

            public void setHigh(int high) {
                this.high = high;
            }

            public long getHighTime() {
                return highTime;
            }

            public void setHighTime(long highTime) {
                this.highTime = highTime;
            }

            public int getLow() {
                return low;
            }

            public void setLow(int low) {
                this.low = low;
            }

            public long getLowTime() {
                return lowTime;
            }

            public void setLowTime(long lowTime) {
                this.lowTime = lowTime;
            }
        }
    }

    public static class Mapping {
        private String examine;
        private int id;
        private boolean members;
        private int lowalch;
        private int limit;
        private int value;
        private int highalch;
        private String icon;
        private String name;

        // Getters and setters for each field

        public String getExamine() {
            return examine;
        }

        public void setExamine(String examine) {
            this.examine = examine;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean getMembers() {
            return members;
        }

        public void setMembers(boolean members) {
            this.members = members;
        }

        public int getLowalch() {
            return lowalch;
        }

        public void setLowalch(int lowalch) {
            this.lowalch = lowalch;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getHighalch() {
            return highalch;
        }

        public void setHighalch(int highalch) {
            this.highalch = highalch;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public int getIdFromString(String input) {
        try {
            Mapping mapping = Main.api.mappingMap.get(Integer.parseInt(input));
            if (mapping != null) {
                return mapping.getId();
            }
        } catch (NumberFormatException e) {
            for (Mapping mapping : Main.api.mappingMap.values()) {
                if (mapping.getName().equalsIgnoreCase(input)) {
                    return mapping.getId();
                }
            }
        }
        return -1;
    }
}

