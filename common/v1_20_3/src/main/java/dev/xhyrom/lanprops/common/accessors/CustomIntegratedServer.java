package dev.xhyrom.lanprops.common.accessors;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface CustomIntegratedServer {
    @NotNull Path lan_properties$propertiesPath();

    @NotNull DedicatedServerSettings lan_properties$settings();

    @NotNull DedicatedServerProperties lan_properties$properties();
    @NotNull CustomDedicatedServerProperties lan_properties$customProperties();
}
