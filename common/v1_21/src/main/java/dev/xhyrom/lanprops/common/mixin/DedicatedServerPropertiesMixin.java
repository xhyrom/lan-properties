package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> implements CustomDedicatedServerProperties {
    @Shadow @Final public int serverPort;
    @Unique
    public final boolean lan_properties$hybridMode = this.get("hybrid-mode", true);

    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }

    public int lan_properties$serverPort() {
        return this.serverPort;
    }

    public void lan_properties$serverPort(int port) {
        this.properties.put("server-port", String.valueOf(port));
    }

    @Unique
    public boolean lan_properties$hybridMode() {
        return lan_properties$hybridMode;
    }
}
