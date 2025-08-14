package dev.xhyrom.lanprops.common.accessors;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public interface CustomSettings {
    @NotNull HashMap<String, Class<?>> lan_properties$keys();
}
