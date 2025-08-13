package dev.xhyrom.lanprops.common.accessors;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public interface CustomDedicatedServerProperties {
    @NotNull Properties lan_properties$properties();

    int lan_properties$serverPort();
    void lan_properties$serverPort(int port);

    boolean lan_properties$hybridMode();
}
