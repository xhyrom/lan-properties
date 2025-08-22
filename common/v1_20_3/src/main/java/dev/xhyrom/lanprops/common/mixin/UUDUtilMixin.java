package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomIntegratedServer;
import dev.xhyrom.lanprops.common.hybrid.HybridMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(UUIDUtil.class)
public class UUDUtilMixin {
    @Inject(method = "createOfflinePlayerUUID", at = @At("HEAD"), cancellable = true)
    private static void createOfflinePlayerUUID(String name, CallbackInfoReturnable<UUID> cir) {
        if (Minecraft.getInstance().getSingleplayerServer() == null)
            return;

        if (!((CustomIntegratedServer) Minecraft.getInstance().getSingleplayerServer()).lan_properties$customProperties().lan_properties$hybridMode())
            return;

        final UUID uuid = HybridMode.onlinePlayerUuid(name);
        if (uuid != null) {
            cir.setReturnValue(uuid);
            cir.cancel();
        }
    }
}
