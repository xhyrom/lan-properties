package dev.xhyrom.lanprops;

import dev.xhyrom.lanprops.common.LanPropertiesClient;
import net.neoforged.fml.common.Mod;

@Mod(LanPropertiesClient.MOD_ID)
public class LanPropertiesClientNeoForge {
    public LanPropertiesClientNeoForge() {
        LanPropertiesClient.init();
    }
}
