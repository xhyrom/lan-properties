package dev.xhyrom.lanprops.common.screens;

import com.google.common.collect.ImmutableList;
import dev.xhyrom.lanprops.common.LanPropertiesClient;
import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import dev.xhyrom.lanprops.common.accessors.CustomSettings;
import dev.xhyrom.lanprops.common.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class PropertiesList extends ContainerObjectSelectionList<PropertiesList.Entry> {
    private final DedicatedServerProperties serverProperties;

    public PropertiesList(PropertiesScreen screen, Minecraft minecraft, DedicatedServerProperties serverProperties) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);

        this.serverProperties = serverProperties;
        this.populateList();
    }

    private void populateList() {
        record PropertyData(String name, Class<?> type) {}

        ((CustomSettings) serverProperties).lan_properties$keys().entrySet().stream()
                .map(entry -> new PropertyData(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PropertyData::name))
                .forEach(data -> {
                    try {
                        this.addEntry(new PropertyEntry(data.name(), data.type()));
                    } catch (IllegalAccessException e) {
                        LanPropertiesClient.LOGGER.error("Failed to create property entry for '{}'", data.name(), e);
                    }
                });

    }

    @Override
    public int getRowWidth() {
        return 360;
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> { }

    public class PropertyEntry extends Entry {
        private final Component propertyName;
        private final AbstractWidget editWidget;
        private static final int PADDING = 160;

        public PropertyEntry(String propertyKey, Class<?> type) throws IllegalAccessException {
            this.propertyName = Component.literal(StringUtils.kebabCaseToTitleCase(propertyKey));

            final Properties properties = ((CustomDedicatedServerProperties) serverProperties).lan_properties$properties();

            final String currentValue = (String) properties.get(propertyKey);
            this.editWidget = createWidget(type, currentValue, (raw, serialized) -> {
                properties.put(propertyKey, serialized);

                LanPropertiesClient.LOGGER.info("Updated property '{}' to '{}'", propertyKey, serialized);
            });
        }

        private AbstractWidget createWidget(Class<?> type, String value, BiConsumer<Object, String> callback) {
            if (type == boolean.class) {
                return CycleButton.onOffBuilder()
                        .withInitialValue(Boolean.parseBoolean(value))
                        .displayOnlyValue()
                        .create(0, 0, 150, 20, Component.empty(), (btn, val) -> callback.accept(val, val.toString()));
            }

            if (type == int.class) {
                final EditBox editBox = new EditBox(minecraft.font, 0, 0, 148, 18, Component.empty());

                editBox.setValue(value);
                editBox.setResponder(str -> {
                    if (str.matches("-?\\d+")) {
                        callback.accept(Integer.parseInt(str), str);
                        editBox.setTextColor(-2039584);
                    } else {
                        editBox.setTextColor(-236718492);
                    }
                });

                return editBox;
            }

            if (type == long.class) {
                final EditBox editBox = new EditBox(minecraft.font, 0, 0, 148, 18, Component.empty());

                editBox.setValue(value);
                editBox.setResponder(str -> {
                    if (str.matches("-?\\d+")) {
                        callback.accept(Long.parseLong(str), str);
                        editBox.setTextColor(-2039584);
                    } else {
                        editBox.setTextColor(-236718492);
                    }
                });

                return editBox;
            }

            if (type.isEnum()) {
                if (type == Difficulty.class) {
                    return CycleButton.builder(Difficulty::getDisplayName)
                            .withValues(Difficulty.values())
                            .withInitialValue(Difficulty.byName(value))
                            .displayOnlyValue()
                            .create(0, 0, 150, 20, Component.empty(), (btn, val) -> callback.accept(val, val.getSerializedName()));
                }

                if (type == GameType.class) {
                    return CycleButton.builder(GameType::getShortDisplayName)
                            .withValues(GameType.values())
                            .withInitialValue(GameType.byName(value))
                            .displayOnlyValue()
                            .create(0, 0, 150, 20, Component.empty(), (btn, val) -> callback.accept(val, val.getName()));
                }
            }

            EditBox editBox = new EditBox(minecraft.font, 0, 0, 148, 18, Component.empty());
            if (value != null)
                editBox.setValue(value);

            editBox.setResponder((str) -> callback.accept(str, str));

            return editBox;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            guiGraphics.drawString(minecraft.font, this.propertyName, left, top + height / 2 - 9 / 2, -1);

            this.editWidget.setX(left + PADDING);
            this.editWidget.setY(top);
            this.editWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editWidget);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editWidget);
        }
    }
}
