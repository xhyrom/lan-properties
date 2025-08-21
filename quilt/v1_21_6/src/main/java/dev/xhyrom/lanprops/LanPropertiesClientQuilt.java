package dev.xhyrom.lanprops;

import dev.xhyrom.lanprops.common.LanPropertiesClient;
import net.fabricmc.api.ModInitializer;

public class LanPropertiesClientQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        LanPropertiesClient.init();
    }
}
