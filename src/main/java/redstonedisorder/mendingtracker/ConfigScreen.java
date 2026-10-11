package redstonedisorder.mendingtracker;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigOptions draft;

    protected ConfigScreen(Screen parent) {
        super(Component.translatable("mending-tracker.config.title"));
        this.parent = parent;
        this.draft = ConfigHelper.configOptions.normalized();
    }

    @Override
    protected void init() {
        MendingTracker.previewOptions = draft;
        addRenderableWidget(positionSlider(true, width / 8, height / 8));
        addRenderableWidget(positionSlider(false, width / 8, height / 4));

        addRenderableWidget(Button.builder(alignmentMessage(), button -> {
            var alignments = ConfigOptions.Alignment.values();
            draft.textAlignment = alignments[(draft.textAlignment.ordinal() + 1) % alignments.length];
            button.setMessage(alignmentMessage());
        }).bounds(17 * width / 32, height / 8, 11 * width / 32, Button.DEFAULT_HEIGHT).build());

        Button overrideButton = Button.builder(overrideMessage(), button -> {
            draft.overrideMinihud = !draft.overrideMinihud;
            button.setMessage(overrideMessage());
        }).bounds(17 * width / 32, height / 4, 11 * width / 32, Button.DEFAULT_HEIGHT).build();
        if (!FabricLoader.getInstance().isModLoaded("minihud")) {
            overrideButton.active = false;
            overrideButton.setTooltip(Tooltip.create(Component.translatable("mending-tracker.config.minihud_missing")));
        }
        addRenderableWidget(overrideButton);

        addRenderableWidget(Button.builder(Component.translatable("mending-tracker.config.save"), button -> {
            if (ConfigHelper.saveConfig(draft)) {
                onClose();
            } else {
                minecraft.gui.toastManager().addToast(new SystemToast(
                        SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                        Component.translatable("mending-tracker.config.error"),
                        Component.translatable("mending-tracker.config.save_failed")));
            }
        }).bounds(5 * width / 16, 3 * height / 8, 3 * width / 8, Button.DEFAULT_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("mending-tracker.config.discard"), button -> onClose())
                .bounds(5 * width / 16, 15 * height / 32, 3 * width / 8, Button.DEFAULT_HEIGHT).build());
    }

    private AbstractSliderButton positionSlider(boolean horizontal, int x, int y) {
        String key = horizontal ? "mending-tracker.config.x" : "mending-tracker.config.y";
        int position = horizontal ? draft.xPos : draft.yPos;
        return new AbstractSliderButton(x, y, 11 * width / 32, Button.DEFAULT_HEIGHT,
                Component.translatable(key, position), position / 100.0) {
            @Override
            protected void updateMessage() { setMessage(Component.translatable(key, Math.round(value * 100))); }

            @Override
            protected void applyValue() {
                int position = (int) Math.round(value * 100);
                if (horizontal) draft.xPos = position;
                else draft.yPos = position;
            }
        };
    }

    private Component alignmentMessage() {
        return Component.translatable("mending-tracker.config.alignment",
                Component.translatable("mending-tracker.alignment." + draft.textAlignment.name()));
    }

    private Component overrideMessage() {
        return Component.translatable("mending-tracker.config.override",
                Component.translatable(draft.overrideMinihud ? "options.on" : "options.off"));
    }

    @Override
    public void onClose() {
        MendingTracker.previewOptions = null;
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void removed() { MendingTracker.previewOptions = null; }
}