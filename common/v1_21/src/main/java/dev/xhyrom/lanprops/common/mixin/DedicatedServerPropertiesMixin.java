package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> implements CustomDedicatedServerProperties {
    @Shadow @Final public int serverPort;

    @Shadow @Final public boolean onlineMode;
    @Shadow @Final public String motd;
    @Shadow @Final public boolean enforceWhitelist;
    @Shadow @Final public boolean preventProxyConnections;
    @Shadow @Final public Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout;
    @Unique
    public final boolean lan_properties$hybridMode = this.get("hybrid-mode", true);

    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }

    public @NotNull Properties lan_properties$properties() {
        return this.properties;
    }

    public boolean lan_properties$onlineMode() {
        return this.get("online-mode", this.onlineMode);
    }

    public boolean lan_properties$preventProxyConnections() {
        return this.get("prevent-proxy-connections", this.preventProxyConnections);
    }

    public boolean lan_properties$pvp() {
        return this.get("pvp", true);
    }

    public boolean lan_properties$allowFlight() {
        return this.get("allow-flight", false);
    }

    public String lan_properties$motd() {
        return this.get("motd", this.motd);
    }

    public int lan_properties$playerIdleTimeout() {
        return this.get("player-idle-timeout", this.playerIdleTimeout.get());
    }

    public boolean lan_properties$enforceWhitelist() {
        return this.get("enforce-whitelist", this.enforceWhitelist);
    }

    public int lan_properties$serverPort() {
        return this.get("server-port", this.serverPort);
    }

    public void lan_properties$serverPort(int port) {
        this.properties.put("server-port", String.valueOf(port));
    }

    @Unique
    public boolean lan_properties$hybridMode() {
        return this.get("hybrid-mode", this.lan_properties$hybridMode);
    }
}
