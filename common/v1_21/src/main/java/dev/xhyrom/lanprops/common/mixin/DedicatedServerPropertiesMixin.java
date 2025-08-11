package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> implements CustomDedicatedServerProperties {
    @Unique
    public final boolean lan_properties$hybridMode = this.get("hybrid-mode", true);

    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }

    @Unique
    public boolean lan_properties$hybridMode() {
        return lan_properties$hybridMode;
    }
}
