package dev.xhyrom.lanprops;

import dev.xhyrom.lanprops.common.LanPropertiesClient;
import net.minecraftforge.fml.common.Mod;

@Mod(LanPropertiesClient.MOD_ID)
public class LanPropertiesClientForge {
    public LanPropertiesClientForge() {
        LanPropertiesClient.init();
    }
}
