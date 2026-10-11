package redstonedisorder.mendingtracker;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.nio.file.Files;
import java.io.IOException;
import net.minecraft.client.gui.screens.DeathScreen;

/** Exercises real 26.3 world, client ticks, widgets and optional mixins. */
public final class MendingTrackerClientTest implements FabricClientGameTest {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try { runChecks(context); }
        catch (Exception e) { throw new AssertionError("Client integration check failed", e); }
    }

    private void runChecks(ClientGameTestContext context) throws Exception {
        checkProfile();
        if (Boolean.getBoolean("mendingtracker.test.restart")) {
            context.runOnClient(client -> MiniHudChecks.checkPersisted());
            return;
        }
        context.runOnClient(client -> {
            check(MendingTracker.totals().equals(MendingTotals.EMPTY), "title screen totals");
            check(!MendingTracker.shouldRenderStandalone(client), "no HUD outside a world");
        });
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var mending = player.registryAccess().getOrThrow(Enchantments.MENDING);
                for (int i = 0; i < SLOTS.length; i++) {
                    var item = switch (SLOTS[i]) {
                        case HEAD -> Items.DIAMOND_HELMET;
                        case CHEST -> Items.DIAMOND_CHESTPLATE;
                        case LEGS -> Items.DIAMOND_LEGGINGS;
                        case FEET -> Items.DIAMOND_BOOTS;
                        default -> Items.DIAMOND_PICKAXE;
                    };
                    ItemStack stack = new ItemStack(item);
                    stack.enchant(mending, 1);
                    stack.setDamageValue(i + 1);
                    player.setItemSlot(SLOTS[i], stack);
                }
                ItemStack inventoryOnly = new ItemStack(Items.DIAMOND_PICKAXE);
                inventoryOnly.enchant(mending, 1);
                inventoryOnly.setDamageValue(100);
                player.getInventory().setItem(9, inventoryOnly);
                ItemStack withoutMending = new ItemStack(Items.DIAMOND_PICKAXE);
                withoutMending.setDamageValue(50);
                player.getInventory().setItem(1, withoutMending);
            });
            context.waitFor(client -> MendingTracker.totals().equals(new MendingTotals(6, 21)), 600);
            check(MendingTracker.totals().xpRequired() == 11, "XP rounding");
            context.runOnClient(client -> {
                client.debugEntries.setOverlayVisible(false);
                ConfigHelper.configOptions.overrideMinihud = true;
                check(MendingTracker.shouldRenderStandalone(client), "standalone enabled");
                client.debugEntries.setOverlayVisible(true);
                check(!MendingTracker.shouldRenderStandalone(client), "debug overlay hides HUD");
                client.debugEntries.setOverlayVisible(false);
                var textEntry = DebugScreenEntries.allEntries().entrySet().stream()
                        .filter(entry -> entry.getValue().category() == DebugEntryCategory.SCREEN_TEXT).findFirst().orElseThrow();
                var previous = client.debugEntries.getStatus(textEntry.getKey());
                client.debugEntries.setStatus(textEntry.getKey(), DebugScreenEntryStatus.ALWAYS_ON);
                check(!MendingTracker.shouldRenderStandalone(client), "always-on debug text hides HUD");
                client.debugEntries.setStatus(textEntry.getKey(), previous);
                client.gui.hud.toggle();
                check(!MendingTracker.shouldRenderStandalone(client), "F1 hides HUD");
                client.gui.hud.toggle();
            });

            context.takeScreenshot("standalone-six-items");
            for (var alignment : ConfigOptions.Alignment.values()) {
                context.runOnClient(client -> {
                    ConfigHelper.configOptions.textAlignment = alignment;
                    ConfigHelper.configOptions.xPos = 50;
                    ConfigHelper.configOptions.yPos = 20;
                });
                context.takeScreenshot("alignment-" + alignment.name());
            }
            context.runOnClient(client -> {
                client.options.guiScale().set(2);
                client.resizeGui();
            });
            context.takeScreenshot("gui-scale-two");

            // Repairing equipped gear updates the next observed client tick.
            world.getServer().runOnServer(server ->
                    server.getPlayerList().getPlayers().getFirst().getMainHandItem().setDamageValue(0));
            context.waitFor(client -> MendingTracker.totals().equals(new MendingTotals(5, 16)), 600);
            context.runOnClient(client -> client.player.getInventory().setSelectedSlot(1));
            context.waitTicks(2);
            context.runOnClient(client -> check(MendingTracker.totals().damagedItems() == 5,
                    "non-mending hotbar item excluded"));
            context.runOnClient(client -> client.player.getInventory().setSelectedSlot(0));

            testConfiguration(context);
            if (FabricLoader.getInstance().isModLoaded("modmenu")) testModMenu(context);
            if (FabricLoader.getInstance().isModLoaded("minihud")) MiniHudChecks.run(context);

            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                for (var slot : SLOTS) player.setItemSlot(slot, ItemStack.EMPTY);
            });
            context.waitFor(client -> MendingTracker.totals().equals(MendingTotals.EMPTY), 600);
            context.takeScreenshot("zero-items");
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.hurtServer(player.level(), player.damageSources().genericKill(), Float.MAX_VALUE);
            });
            context.waitForScreen(DeathScreen.class);
            context.clickScreenButton("deathScreen.respawn");
            context.waitFor(client -> client.player != null && client.player.isAlive() && client.gui.screen() == null, 600);
            context.waitTick();
            check(MendingTracker.totals().equals(MendingTotals.EMPTY), "respawn has no stale equipment totals");
        }
        context.waitTick();
        context.runOnClient(client -> {
            check(MendingTracker.totals().equals(MendingTotals.EMPTY), "disconnect clears totals");
            check(!MendingTracker.shouldRenderStandalone(client), "disconnect hides HUD");
        });
        // A fresh world must not inherit the previous world's equipment.
        try (var ignored = context.worldBuilder().create()) {
            context.waitTicks(2);
            check(MendingTracker.totals().equals(MendingTotals.EMPTY), "reconnect starts with zero totals");
        }
    }

    private static void testConfiguration(ClientGameTestContext context) throws IOException {
        context.runOnClient(client -> check(ConfigHelper.saveConfig(ConfigHelper.configOptions), "initial config save"));
        context.setScreen(() -> new ConfigScreen(null));
        clickTranslatedButton(context, "mending-tracker.config.alignment");
        var draftAlignment = context.computeOnClient(client -> MendingTracker.renderOptions().textAlignment);
        context.runOnClient(client -> client.resizeGui());
        check(context.computeOnClient(client -> MendingTracker.renderOptions().textAlignment) == draftAlignment,
                "resize retains unsaved edits");
        context.clickScreenButton("mending-tracker.config.discard");

        // Make the destination unwritable without relying on platform-specific file permissions.
        var path = FabricLoader.getInstance().getConfigDir().resolve("mending-tracker.json");
        var backup = path.resolveSibling("saved-config-for-test.json");
        Files.move(path, backup);
        Files.createDirectory(path);
        var blocker = path.resolve("blocker");
        Files.writeString(blocker, "occupied directory");
        try {
            context.setScreen(() -> new ConfigScreen(null));
            var before = ConfigHelper.configOptions.textAlignment;
            clickTranslatedButton(context, "mending-tracker.config.alignment");
            context.clickScreenButton("mending-tracker.config.save");
            context.runOnClient(client -> {
                check(client.gui.screen() instanceof ConfigScreen, "failed save remains open");
                check(ConfigHelper.configOptions.textAlignment == before, "failed save preserves active config");
            });
            context.clickScreenButton("mending-tracker.config.discard");
        } finally {
            Files.delete(blocker);
            Files.delete(path);
            Files.move(backup, path);
        }
        context.runOnClient(client -> check(MendingTracker.previewOptions == null, "discard clears preview"));

        context.setScreen(() -> new ConfigScreen(null));
        clickTranslatedButton(context, "mending-tracker.config.alignment");
        var savedAlignment = context.computeOnClient(client -> MendingTracker.renderOptions().textAlignment);
        context.clickScreenButton("mending-tracker.config.save");
        context.runOnClient(client -> {
            ConfigHelper.getConfigOptions();
            check(ConfigHelper.configOptions.textAlignment == savedAlignment, "saved config reloads");
        });
        context.setScreen(() -> new ConfigScreen(null));
        context.runOnClient(client -> client.gui.screen().keyPressed(
                new KeyEvent(InputConstants.KEY_ESCAPE, 0, 0)));
        context.runOnClient(client -> check(MendingTracker.previewOptions == null, "Escape discards preview"));

        // Exercise the command through the client command dispatcher.
        context.runOnClient(client -> client.player.connection.sendCommand("mendingTracker"));
        context.waitForScreen(ConfigScreen.class);
        context.takeScreenshot("configuration");
        context.clickScreenButton("mending-tracker.config.discard");
    }

    private static void testModMenu(ClientGameTestContext context) throws ReflectiveOperationException {
        context.runOnClient(client -> {
            var apiClass = Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
            var parent = (net.minecraft.client.gui.screens.Screen)
                    apiClass.getMethod("createModsScreen", net.minecraft.client.gui.screens.Screen.class).invoke(null, new Object[]{null});
            var integration = new ModMenuIntegration();
            client.gui.setScreen(integration.getModConfigScreenFactory().create(parent));
            client.gui.screen().onClose();
            check(client.gui.screen() == parent, "Mod Menu parent restored");
            client.gui.setScreen(null);
        });
    }

    private static void clickTranslatedButton(ClientGameTestContext context, String key) {
        context.runOnClient(client -> {
            Button button = client.gui.screen().children().stream().filter(Button.class::isInstance)
                    .map(Button.class::cast).filter(widget -> widget.getMessage().getContents() instanceof TranslatableContents text
                            && text.getKey().equals(key)).findFirst().orElseThrow();
            button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
        });
    }

    private static void checkProfile() {
        String profile = System.getProperty("mendingtracker.test.profile", "none");
        var loader = FabricLoader.getInstance();
        if (Boolean.getBoolean("mendingtracker.test.packaged")) {
            check(!loader.isDevelopmentEnvironment(), "production environment");
            check(MendingTracker.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"),
                    "main mod loaded from packaged JAR");
        }
        check(loader.isModLoaded("modmenu") == (profile.equals("modmenu") || profile.equals("all")), "Mod Menu profile");
        check(loader.isModLoaded("minihud") == (profile.equals("minihud") || profile.equals("all")), "MiniHUD profile");
        check(loader.isModLoaded("malilib") == (!profile.equals("none") && !profile.equals("modmenu")), "MaLiLib profile");
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            MendingTracker.LOGGER.error("Client integration assertion failed: {}", message);
            throw new AssertionError(message);
        }
    }
}
