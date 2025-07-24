package dev.xhyrom.lanprops;

import net.minecraftforge.fml.common.Mod;

@Mod(LanPropertiesClient.MOD_ID)
public class LanPropertiesClientForge {
    public LanPropertiesClientForge() {
        LanPropertiesClient.init();
    }
}
