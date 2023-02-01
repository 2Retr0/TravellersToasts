package retr0.travellerstoasts.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static retr0.travellerstoasts.config.CarrotConfig.configEntries;

public class CarrotConfigScreen extends Screen {
    protected static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public final String translationPrefix;
    public final Screen parent;
    public final String modId;
    public ButtonWidget doneButton;
    private final Set<AbstractConfigEntry> invalidEntries = new HashSet<>();
    private AbstractConfigEntry activeEntry;

    public ConfigEntryList entries;
    private List<? extends OrderedText> tooltip;
    private Map<AbstractConfigEntry, Field> entryMap = new HashMap<>();

    protected CarrotConfigScreen(Screen parent, String modId) {
        super(Text.translatable(modId + ".config.title"));

        this.parent = parent;
        this.modId = modId;
        this.translationPrefix = modId + ".config.";
    }

    @Override
    protected final void init() {
        entries = new ConfigEntryList(width, height, 32 ,height - 32, 25);

        configEntries.forEach(entryInfo -> {
            var defaultValue = entryInfo.defaultValue();
            AbstractConfigEntry entry = null; // TOOD: NULL CHECK IS BAD

            Object value = null;
            try { value = entryInfo.field().get(null); } catch (IllegalAccessException ignored) { }

            // entry = switch (defaultValue) {
            //     case Integer i -> entries.createIntEntry(entryInfo.key(), i, (int) value, entryInfo.isColor());
            //     case Float f   -> entries.createFloatEntry(entryInfo.key(), f, (float) value);
            //     case Boolean b -> entries.createBooleanEntry(entryInfo.key(), b, (boolean) value);
            //     default -> throw new IllegalStateException("Unexpected value: " + defaultValue);
            // };

            if (defaultValue instanceof Integer i)
                entry = entries.createIntEntry(entryInfo.key(), i, (int) value, entryInfo.isColor());
            else if (defaultValue instanceof Float f)
                entry = entries.createFloatEntry(entryInfo.key(), f, (float) value);
            else if (defaultValue instanceof Boolean b)
                entries.createBooleanEntry(entryInfo.key(), b, (boolean) value);

            entry.setValue(value);

            entryMap.put(entry, entryInfo.field());
            entries.addEntry(entry);
        });
        addDrawableChild(entries);

        // TODO: make comments/title with multiline
        addDrawableChild(new ButtonWidget.Builder(ScreenTexts.CANCEL, button -> {
            // TODO
            Objects.requireNonNull(client).setScreen(parent);
        })
            .dimensions(width / 2 - 155 + 160, height - 29, 150, 20)
            .build());

        doneButton = addDrawableChild(new ButtonWidget.Builder(ScreenTexts.DONE, button -> {
            write(modId);
            Objects.requireNonNull(client).setScreen(parent);
        })
            .dimensions(width / 2 - 155, height - 29, 150, 20)
            .build());
    }

    public void write(String modId) {
        entryMap.forEach((entry, field) -> {
            try { field.set(null, entry.getValue()); } catch (IllegalAccessException ignored) { }
        });
        CarrotConfig.write(modId);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredText(matrices, textRenderer, title, width / 2, 10, 0xFFFFFF);
        if (tooltip != null) renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
    }

    @Override
    public void tick() { entries.tick(); }

    private void updateDoneButton() { this.doneButton.active = this.invalidEntries.isEmpty(); }

    private void updateInvalidEntries(AbstractConfigEntry entry, boolean isValid) {
        if (isValid) invalidEntries.remove(entry); else invalidEntries.add(entry);
    }

    public class ConfigEntryList extends ElementListWidget<AbstractConfigEntry> {
        public ConfigEntryList(int width, int height, int top, int bottom, int itemHeight) {
            super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
            super.addEntry(new ConfigCategoryEntry(Text.translatable("TEST").formatted(Formatting.BOLD, Formatting.YELLOW)));
        }

        @Override public int getScrollbarPositionX() { return width - 7; }

        public void tick() { children().forEach(AbstractConfigEntry::tick); }

        private static Function<String, Object> createParser(Function<String, Object> parser) {
            return value -> {
                try {
                    return parser.apply(value);
                } catch (Exception ignored) { }
                return null;
            };
        }

        public int addEntry(AbstractConfigEntry entry) {
            return super.addEntry(entry);
        }

        public AbstractConfigEntry createIntEntry(String key, int defaultValue, int initValue, boolean isColor) {
            var intParser = createParser(isColor ? value -> Integer.parseInt(value.substring(1), 16) : Integer::parseInt);
            Function<Object, Text> intTextProvider = isColor ?
                value -> Text.literal("#" + Integer.toHexString((int) value).toUpperCase()) :
                value -> Text.literal(String.valueOf((int) value));

            var textEntry = new ConfigTextEntry(key, intParser, width, defaultValue, initValue, intTextProvider);
            if (isColor) {
                textEntry.textField.setMaxLength(7);
                textEntry.textField.setTextPredicate(value -> value.startsWith("#"));
                textEntry.textField.setRenderTextProvider((string, index) ->
                    OrderedText.styledForwardsVisitedString(string.toUpperCase(), Style.EMPTY));
            }

            return textEntry;
        }

        public AbstractConfigEntry createFloatEntry(String key, float defaultValue, float initValue) {
            var floatParser = createParser(Float::parseFloat);
            Function<Object, Text> floatTextProvider = value -> Text.literal(String.valueOf((float) value));

            return new ConfigTextEntry(key, floatParser, width, defaultValue, initValue, floatTextProvider);
        }

        public AbstractConfigEntry createBooleanEntry(String key, boolean defaultValue, boolean initValue) {
            Function<Object, Text> textProvider = value ->
                Text.translatable((boolean) value ? "gui.yes" : "gui.no")
                    .formatted((boolean) value ? Formatting.GREEN : Formatting.RED);

            var buttonEntry = new ConfigEntry(key, width, defaultValue, textProvider);

            buttonEntry.children.add(new ButtonWidget.Builder(textProvider.apply(initValue),
                button -> {
                    buttonEntry.setValue(!(boolean) buttonEntry.getValue());
                    button.setMessage(textProvider.apply(buttonEntry.getValue()));
                })
                .dimensions(width - 160, 0, 150, 20)
                .build());

            return buttonEntry;
        }

        @Override public int getRowWidth() { return 10000; }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            var configEntry = (AbstractConfigEntry) getHoveredEntry();
            CarrotConfigScreen.this.setTooltip(configEntry != null ? configEntry.tooltip : null);
        }

        /**
         * Deselects any entries which are not entry at the click position (consequently deselects <em>all</em> entries
         * if no entries exist at the click position).
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            var hoveredEntry = (AbstractConfigEntry) getHoveredEntry();
            if (activeEntry != hoveredEntry && activeEntry instanceof ConfigTextEntry textEntry)
                textEntry.textField.setTextFieldFocused(false);
            activeEntry = hoveredEntry;

            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public static class ConfigEntry extends AbstractConfigEntry {
        protected final List<ClickableWidget> children = new ArrayList<>();
        protected final Text name;
        protected final Object defaultValue;
        protected final Function<Object, Text> textProvider;
        protected final int width;

        private Object value;

        private ConfigEntry(String translationKey, int width, Object defaultValue, Function<Object, Text> textProvider) {
            name = Text.translatable(translationKey);
            this.defaultValue = defaultValue;
            this.textProvider = textProvider;
            this.width = width;
            this.value = defaultValue;

            //*** TOOLTIP SETUP ***//
            // We have to add a name, as multi-line tooltips always have a small spacing between lines 1 and 2.
            var translationName = translationKey.substring(translationKey.lastIndexOf('.') + 1);
            tooltip.add(Text.literal(translationName).formatted(Formatting.YELLOW).asOrderedText());

            var descriptionKey = translationKey + ".tooltip";
            if (!Text.translatable(descriptionKey).getString().equals(descriptionKey))
                tooltip.addAll(textRenderer.wrapLines(Text.translatable(descriptionKey), 260));

            var defaultText = Text.literal(textProvider.apply(defaultValue).getString());
            tooltip.add(Text.translatable("editGamerule.default", defaultText).formatted(Formatting.GRAY).asOrderedText());

            //*** RESET BUTTON SETUP ***//
            children.add(new ButtonWidget.Builder(Text.translatable("Reset").formatted(Formatting.RED),
                button -> {
                    setValue(defaultValue);
                    children.subList(1, children.size()).forEach(widget -> {
                        if (widget instanceof TextFieldWidget textField)
                            textField.setText(textProvider.apply(defaultValue).getString());
                        else if (widget instanceof ButtonWidget button2)
                            button2.setMessage(textProvider.apply(defaultValue));
                    });
                }).dimensions(width - 205, 0, 40, 20).build());
        }

        @Override
        public Object getValue() { return value; }

        @Override
        public boolean setValue(Object value) {
            if (value != null) this.value = value;
            return value != null;
        }

        @Override
        public void render(
            MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
            boolean hovered, float tickDelta)
        {
            children.forEach(b -> { b.setY(y); b.render(matrices, mouseX, mouseY, tickDelta); });
            DrawableHelper.drawTextWithShadow(matrices, textRenderer, name, 12, y + 5, 0xFFFFFF);
        }

        @Override
        public void tick() {
            // Ensure reset button is deactivated if the value is not the default value.
            children.get(0).active = !value.equals(defaultValue);
            // Tick any widgets which are textFieldWidgets to allow input update.
            children.forEach(widget -> { if (widget instanceof TextFieldWidget textField) textField.tick(); });
        }

        @Override public List<? extends Selectable> selectableChildren() { return children; }
        @Override public List<? extends Element> children() { return children; }
    }

    public class ConfigTextEntry extends ConfigEntry {
        protected final TextFieldWidget textField;
        private final String defaultString;

        private ConfigTextEntry(String translationKey, Function<String, Object> validator, int width, Object defaultValue, Object initValue, Function<Object, Text> textProvider) {
            super(translationKey, width, defaultValue, textProvider);

            defaultString = textProvider.apply(defaultValue).getString();
            textField = new TextFieldWidget(textRenderer, width - 160, 0, 150, 20, null);

            textField.setText(textProvider.apply(initValue).getString());
            textField.setChangedListener(value -> {
                var wasParsed = setValue(validator.apply(value));

                textField.setEditableColor(wasParsed ? Formatting.WHITE.getColorValue() : Formatting.RED.getColorValue());
                CarrotConfigScreen.this.updateInvalidEntries(this, wasParsed);
                CarrotConfigScreen.this.updateDoneButton();
            });

            children.add(textField);
        }

        @Override
        public void render(
            MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
            boolean hovered, float tickDelta)
        {
            super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            // We add a square character at the end of the text field to give visual feedback on the last valid color.
            if (defaultString.startsWith("#"))
                DrawableHelper.drawTextWithShadow(matrices, textRenderer, Text.literal("⬛"), width - 22, y + 5, (int) getValue());
        }
    }

    public static class ConfigCategoryEntry extends AbstractConfigEntry {
        protected static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        final Text name;

        public ConfigCategoryEntry(Text text) { this.name = text; }

        @Override
        public void render(
            MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY,
            boolean hovered, float tickDelta)
        {
            DrawableHelper.drawCenteredText(matrices, textRenderer, this.name, x + entryWidth / 2, y + 5, 0xFFFFFF);
        }
    }

    public abstract static class AbstractConfigEntry extends ElementListWidget.Entry<AbstractConfigEntry> {
        public final List<OrderedText> tooltip = new ArrayList<>();

        public AbstractConfigEntry() { }

        public void tick() { }

        public Object getValue() { return null; }

        public boolean setValue(Object value) { return false; }

        @Override public List<? extends Selectable> selectableChildren() { return List.of(); }
        @Override public List<? extends Element> children() { return List.of(); }
    }
}
