package dev.xhyrom.lanprops.common.mixin;

import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import dev.xhyrom.lanprops.common.accessors.CustomIntegratedServer;
import dev.xhyrom.lanprops.common.screens.PropertiesScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShareToLanScreen.class)
public class ShareToLanScreenMixin extends Screen {
    @Unique
    private static final Component LAN_PROPERTIES = Component.translatable("lan_properties.gui.properties");

    @Shadow private int port;

    protected ShareToLanScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void onInit(CallbackInfo ci) {
        assert this.minecraft != null;
        final CustomIntegratedServer server = (CustomIntegratedServer) this.minecraft.getSingleplayerServer();

        assert server != null;
        final CustomDedicatedServerProperties properties = server.lan_properties$customProperties();

        if (HttpUtil.isPortAvailable(properties.lan_properties$serverPort()))
            this.port = properties.lan_properties$serverPort();

        this.addRenderableWidget(
                Button.builder(LAN_PROPERTIES, (btn) -> this.minecraft.setScreen(new PropertiesScreen(this)))
                        .bounds(this.width / 2 - 75, 190, 150, 20)
                        .build()
        );
    }
}
