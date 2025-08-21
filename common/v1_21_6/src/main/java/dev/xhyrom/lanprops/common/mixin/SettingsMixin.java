package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomSettings;
import net.minecraft.server.dedicated.Settings;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(Settings.class)
public class SettingsMixin implements CustomSettings {
    @Unique
    private static final HashMap<String, Class<?>> lan_properties$KEYS = new HashMap<>();

    @Inject(method = "get(Ljava/lang/String;I)I", at = @At("HEAD"))
    private void get(String key, int value, CallbackInfoReturnable<Integer> cir) {
        lan_properties$KEYS.put(key, int.class);
    }

    @Inject(method = "get(Ljava/lang/String;J)J", at = @At("HEAD"))
    private void get(String key, long value, CallbackInfoReturnable<Long> cir) {
        lan_properties$KEYS.put(key, long.class);
    }

    @Inject(method = "get(Ljava/lang/String;Z)Z", at = @At("HEAD"))
    private void get(String key, boolean value, CallbackInfoReturnable<Boolean> cir) {
        lan_properties$KEYS.put(key, boolean.class);
    }

    @Inject(method = "get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"))
    private void get(String key, String value, CallbackInfoReturnable<String> cir) {
        lan_properties$KEYS.put(key, String.class);
    }

    @Inject(method = "getLegacyBoolean", at = @At("HEAD"))
    private void getLegacyBoolean(String key, CallbackInfoReturnable<Boolean> cir) {
        lan_properties$KEYS.put(key, Boolean.class);
    }

    @Inject(method = "getLegacyString", at = @At("HEAD"))
    private void getLegacyString(String key, CallbackInfoReturnable<String> cir) {
        lan_properties$KEYS.put(key, String.class);
    }

    @Override
    public @NotNull HashMap<String, Class<?>> lan_properties$keys() {
        return lan_properties$KEYS;
    }
}
