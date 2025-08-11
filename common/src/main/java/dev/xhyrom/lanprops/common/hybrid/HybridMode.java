package dev.xhyrom.lanprops.common.hybrid;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.xhyrom.lanprops.common.AbstractLanPropertiesClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public final class HybridMode {
    public static UUID onlinePlayerUuid(final String playerName) {
        AbstractLanPropertiesClient.LOGGER.info("Hybrid mode: fetching UUID for player " + playerName);

        try {
            final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String id = json.get("id").getAsString();

            return UUID.fromString(id.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"));
        } catch (Exception e) {
            AbstractLanPropertiesClient.LOGGER.error("Hybrid mode: error fetching UUID for player " + playerName, e);
        }

        return null;
    }
}
