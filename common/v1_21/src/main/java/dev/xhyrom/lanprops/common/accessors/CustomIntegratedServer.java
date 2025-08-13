package dev.xhyrom.lanprops.common.accessors;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.jetbrains.annotations.NotNull;

public interface CustomIntegratedServer {
    @NotNull DedicatedServerSettings lan_properties$settings();

    @NotNull DedicatedServerProperties lan_properties$properties();
    @NotNull CustomDedicatedServerProperties lan_properties$customProperties();
}
