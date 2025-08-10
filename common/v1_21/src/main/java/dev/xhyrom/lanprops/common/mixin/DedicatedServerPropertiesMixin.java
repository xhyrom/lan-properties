package dev.xhyrom.lanprops.common.mixin;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> {
    public final boolean hybridMode = this.get("hybrid-mode", true);

    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }
}
