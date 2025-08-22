package dev.xhyrom.lanprops.common.screens;

import dev.xhyrom.lanprops.common.accessors.CustomIntegratedServer;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.jetbrains.annotations.Nullable;

public class PropertiesScreen extends Screen {
    private static final Component TITLE = Component.translatable("lan_properties.gui.properties");

    @Nullable
    private final Screen lastScreen;
    private DedicatedServerSettings serverSettings;

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private PropertiesList propertiesList;

    public PropertiesScreen(@Nullable Screen screen) {
        super(TITLE);

        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        final CustomIntegratedServer server = (CustomIntegratedServer) this.minecraft.getSingleplayerServer();
        this.serverSettings = server.lan_properties$settings();

        this.layout.addToHeader(new StringWidget(TITLE, font));

        this.propertiesList = this.layout.addToContents(new PropertiesList(this, this.minecraft, serverSettings.getProperties()));

        final LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));

        footerLayout.addChild(Button.builder(Component.translatable("lan_properties.gui.open_world"), (button) -> Util.getPlatform().openUri(server.lan_properties$propertiesPath().getParent().toUri())).build());
        footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.propertiesList != null) {
            this.propertiesList.setSize(this.width, this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight());
            this.propertiesList.setPosition(0, this.layout.getHeaderHeight());
            this.propertiesList.setScrollAmount(this.propertiesList.getScrollAmount());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.propertiesList.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.serverSettings.forceSave();
        this.minecraft.setScreen(this.lastScreen);
    }
}
