package dev.xhyrom.lanprops.common.accessors;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public interface CustomDedicatedServerProperties {
    @NotNull Properties lan_properties$properties();

    boolean lan_properties$onlineMode();
    boolean lan_properties$preventProxyConnections();
    boolean lan_properties$pvp();
    boolean lan_properties$allowFlight();
    String lan_properties$motd();
    int lan_properties$playerIdleTimeout();
    boolean lan_properties$enforceWhitelist();

    int lan_properties$serverPort();
    void lan_properties$serverPort(int port);

    boolean lan_properties$hybridMode();
}
