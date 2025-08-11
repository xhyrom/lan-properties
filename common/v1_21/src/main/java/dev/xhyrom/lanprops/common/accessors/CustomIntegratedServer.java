package dev.xhyrom.lanprops.common.accessors;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.jetbrains.annotations.NotNull;

public interface CustomIntegratedServer {
    @NotNull DedicatedServerProperties lan_properties$properties();
    @NotNull CustomDedicatedServerProperties lan_properties$customProperties();
}
