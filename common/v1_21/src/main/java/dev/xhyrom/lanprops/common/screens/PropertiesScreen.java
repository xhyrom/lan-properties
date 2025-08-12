package dev.xhyrom.lanprops.common.screens;

import dev.xhyrom.lanprops.common.accessors.CustomIntegratedServer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.jetbrains.annotations.Nullable;

public class PropertiesScreen extends Screen {
    private static final Component TITLE = Component.translatable("lan_properties.gui.properties");

    @Nullable
    private final Screen lastScreen;
    private DedicatedServerProperties serverProperties;

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private PropertiesList propertiesList;

    public PropertiesScreen(@Nullable Screen screen) {
        super(TITLE);

        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        this.serverProperties = ((CustomIntegratedServer) this.minecraft.getSingleplayerServer()).lan_properties$properties();

        this.layout.addTitleHeader(TITLE, this.font);

        this.propertiesList = this.layout.addToContents(new PropertiesList(this, this.minecraft, this.serverProperties));

        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));

        footerLayout.addChild(Button.builder(Component.translatable("lan_properties.gui.apply"), (button) -> {
            this.propertiesList.saveChanges();
            this.onClose();
        }).build());

        footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.propertiesList != null) {
            this.propertiesList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.propertiesList.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
