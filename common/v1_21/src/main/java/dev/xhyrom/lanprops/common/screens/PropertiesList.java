package dev.xhyrom.lanprops.common.screens;

import com.google.common.collect.ImmutableList;
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

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class PropertiesList extends ContainerObjectSelectionList<PropertiesList.Entry> {
    private final PropertiesScreen parentScreen;
    private final DedicatedServerProperties serverProperties;

    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("lan_properties.category.general", Arrays.asList(
                "motd", "pvp", "difficulty", "gamemode", "force-gamemode", "hardcore"
        ));
        CATEGORIES.put("lan_properties.category.world", Arrays.asList(
                "allow-flight", "spawn-protection", "level-name", "max-world-size", "allow-nether", "spawn-monsters"
        ));
        CATEGORIES.put("lan_properties.category.network", Arrays.asList(
                "server-port", "online-mode", "max-players", "network-compression-threshold", "player-idle-timeout"
        ));
    }

    public PropertiesList(PropertiesScreen screen, Minecraft minecraft, DedicatedServerProperties serverProperties) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);

        this.parentScreen = screen;
        this.serverProperties = serverProperties;

        this.populateList();
    }

    private void populateList() {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : DedicatedServerProperties.class.getFields()) {
            fieldMap.put(field.getName().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT), field);
        }

        for (Map.Entry<String, List<String>> categoryEntry : CATEGORIES.entrySet()) {
            this.addEntry(new CategoryEntry(Component.translatable(categoryEntry.getKey())));

            for (String propertyName : categoryEntry.getValue()) {
                Field field = fieldMap.get(propertyName);
                if (field != null) {
                    try {
                        this.addEntry(new PropertyEntry(field, propertyName, serverProperties, this::setProperty));
                    } catch (IllegalAccessException e) {
                        System.err.println("Could not access property field: " + propertyName);
                    }
                }
            }
        }
    }

    public void setProperty(String key) {
        System.out.println("Setting property: " + key);
    }

    public void saveChanges() {
        System.out.println("Saving properties...");
    }

    @Override
    public int getRowWidth() {
        return 360;
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component name;

        public CategoryEntry(Component name) {
            this.name = name;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.name, Minecraft.getInstance().screen.width / 2, top + height / 2 - Minecraft.getInstance().font.lineHeight / 2, 0xFFFFFF);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of();
        }
    }

    public class PropertyEntry extends Entry {
        private final Component propertyName;
        private final AbstractWidget editWidget;
        private static final int PADDING = 160;

        public PropertyEntry(Field field, String propertyKey, DedicatedServerProperties props, Consumer<String> updateCallback) throws IllegalAccessException {
            String translationKey = "lan_properties.property." + propertyKey;
            this.propertyName = Component.translatable(translationKey);
            Object currentValue = field.get(props);

            this.editWidget = createWidget(propertyKey, field.getType(), currentValue, updateCallback);
        }

        private AbstractWidget createWidget(String key, Class<?> type, Object value, Consumer<String> callback) {
            if (type == boolean.class) {
                return CycleButton.booleanBuilder(Component.translatable("options.on"), Component.translatable("options.off"))
                        .withInitialValue((Boolean) value)
                        .create(0, 0, 150, 20, Component.literal(""), (btn, val) -> callback.accept(val.toString()));
            }

            if (type == int.class) {
                EditBox editBox = new EditBox(minecraft.font, 0, 0, 148, 18, Component.literal(""));
                editBox.setValue(value.toString());
                editBox.setResponder(str -> {
                    if (str.matches("-?\\d+")) {
                        callback.accept(str);
                        editBox.setTextColor(0xE0E0E0);
                    } else {
                        editBox.setTextColor(0xFF5555);
                    }
                });

                return editBox;
            }

            if (type.isEnum()) {
                if (type == Difficulty.class) {
                    return CycleButton.builder(Difficulty::getDisplayName)
                            .withValues(Difficulty.values())
                            .withInitialValue((Difficulty) value)
                            .create(0, 0, 150, 20, Component.literal(""), (btn, val) -> callback.accept(val.getSerializedName()));
                }
                if (type == GameType.class) {
                    return CycleButton.builder(GameType::getLongDisplayName)
                            .withValues(GameType.values())
                            .withInitialValue((GameType) value)
                            .create(0, 0, 150, 20, Component.literal(""), (btn, val) -> callback.accept(val.getName()));
                }
            }

            EditBox editBox = new EditBox(minecraft.font, 0, 0, 148, 18, Component.literal(""));
            editBox.setValue(value.toString());
            editBox.setMaxLength(1024);
            editBox.setResponder(callback);
            return editBox;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            guiGraphics.drawString(minecraft.font, this.propertyName, left, top + height / 2 - minecraft.font.lineHeight / 2, 0xFFFFFF);

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
