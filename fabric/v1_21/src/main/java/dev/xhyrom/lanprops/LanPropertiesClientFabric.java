package dev.xhyrom.lanprops;

import net.fabricmc.api.ModInitializer;

public class LanPropertiesClientFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LanPropertiesClient.init();
    }
}
