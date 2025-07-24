package dev.xhyrom.lanprops;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public class LanPropertiesClient {
    public static final String MOD_ID = "lan_properties";

    public static final TaggedLogger LOGGER = Logger.tag(MOD_ID);

    public static void init() {
        LOGGER.info("LAN Properties Client initialized.");
    }
}
