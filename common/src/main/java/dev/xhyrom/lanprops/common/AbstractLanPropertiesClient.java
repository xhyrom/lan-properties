package dev.xhyrom.lanprops.common;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public abstract class AbstractLanPropertiesClient {
    public static final String MOD_ID = "lan_properties";
    public static final TaggedLogger LOGGER = Logger.tag(MOD_ID);

    public static void init() {
        LOGGER.info("Lan Properties Mod initialized.");
    }
}
