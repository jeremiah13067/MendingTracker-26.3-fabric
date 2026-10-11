package redstonedisorder.mendingtracker;

import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.InfoToggle;
import fi.dy.masa.minihud.event.RenderHandler;
import fi.dy.masa.minihud.gui.GuiConfigs;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.ChatFormatting;

import java.util.Arrays;
import java.util.List;

/** Loaded only by the MiniHUD profiles; the base test never links optional API types. */
final class MiniHudChecks {
    static void run(ClientGameTestContext context) throws ReflectiveOperationException {
        context.runOnClient(client -> {
            InfoToggle toggle = mendingToggle();
            MendingTrackerClientTest.check(Arrays.stream(InfoToggle.values())
                    .filter(value -> value.name().equals("MENDING_INFO")).count() == 1, "exactly one MiniHUD toggle");
            MendingTrackerClientTest.check(InfoToggle.VALUES.contains(toggle), "MiniHUD persistence includes toggle");
            MendingTrackerClientTest.check(GuiConfigs.INFO_LINE_LIST.contains(toggle), "MiniHUD GUI includes toggle");
            toggle.setBooleanValue(true);
            toggle.setIntegerValue(0);
            toggle.getKeybind().setValueFromString("M");
            Configs.Generic.MAIN_RENDERING_TOGGLE.setBooleanValue(true);
            Configs.saveToFile();
            toggle.setBooleanValue(false);
            toggle.setIntegerValue(7);
            toggle.getKeybind().setValueFromString("");
            Configs.loadFromFile();
            MendingTrackerClientTest.check(toggle.getBooleanValue(), "MiniHUD enabled state persists");
            MendingTrackerClientTest.check(toggle.getIntegerValue() == 0, "MiniHUD line order persists");
            MendingTrackerClientTest.check(toggle.getKeybind().getStringValue().equals("M"), "MiniHUD hotkey persists");
            ConfigHelper.configOptions.overrideMinihud = false;
            MendingTrackerClientTest.check(!MendingTracker.shouldRenderStandalone(client), "MiniHUD owns display");
        });
        context.waitTicks(5);
        context.runOnClient(client -> {
            var field = RenderHandler.class.getDeclaredField("lineWrappers");
            field.setAccessible(true);
            var lines = (List<?>) field.get(RenderHandler.getInstance());
            String expected = "Equipment pieces to mend: " + MendingTracker.totals().damagedItems()
                    + ", XP required: " + MendingTracker.totals().xpRequired();
            boolean found = false;
            for (Object line : lines) {
                var text = line.getClass().getDeclaredField("str");
                text.setAccessible(true);
                found |= expected.equals(ChatFormatting.stripFormatting((String) text.get(line)));
            }
            MendingTrackerClientTest.check(found, "MiniHUD renderer uses the same totals as the standalone HUD");
        });
        context.takeScreenshot("minihud-mending-line");
        context.runOnClient(client -> {
            ConfigHelper.configOptions.overrideMinihud = true;
            MendingTrackerClientTest.check(MendingTracker.shouldRenderStandalone(client), "MiniHUD override");
        });
        context.takeScreenshot("minihud-override");
    }

    private static InfoToggle mendingToggle() {
        return Arrays.stream(InfoToggle.values()).filter(value -> value.name().equals("MENDING_INFO"))
                .findFirst().orElseThrow();
    }
}