package redstonedisorder.mendingtracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MendingTracker implements ClientModInitializer {
    public static final String MOD_ID = "mending-tracker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final EquipmentSlot[] TRACKED_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private static volatile MendingTotals totals = MendingTotals.EMPTY;
    static ConfigOptions previewOptions;
    private static boolean miniHudLoaded;

    public static MendingTotals totals() { return totals; }

    static ConfigOptions renderOptions() {
        return previewOptions == null ? ConfigHelper.configOptions : previewOptions;
    }

    static boolean shouldRenderStandalone(Minecraft client) {
        return client.player != null && client.level != null
                && (!miniHudLoaded || renderOptions().overrideMinihud)
                && !client.gui.hud.isHidden()
                && !client.debugEntries.isOverlayVisible()
                && DebugScreenEntries.allEntries().entrySet().stream().noneMatch(entry ->
                    entry.getValue().category().equals(DebugEntryCategory.SCREEN_TEXT)
                    && client.debugEntries.getStatus(entry.getKey()).equals(DebugScreenEntryStatus.ALWAYS_ON));
    }

    static void updateTotals(Minecraft client) {
        MendingTotals next = MendingTotals.EMPTY;
        if (client.player != null && client.level != null) {
            var mending = client.player.registryAccess().getOrThrow(Enchantments.MENDING);
            for (EquipmentSlot slot : TRACKED_SLOTS) {
                var stack = client.player.getItemBySlot(slot);
                next = next.add(stack.getDamageValue(),
                        EnchantmentHelper.getItemEnchantmentLevel(mending, stack) > 0);
            }
        }
        totals = next;
    }

    @Override
    public void onInitializeClient() {
        ConfigHelper.getConfigOptions();
        miniHudLoaded = FabricLoader.getInstance().isModLoaded("minihud");
        ClientTickEvents.END_CLIENT_TICK.register(MendingTracker::updateTotals);
        Minecraft client = Minecraft.getInstance();

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "mending_overlay"), (graphics, tickCounter) -> {
            if (!shouldRenderStandalone(client)) return;
            MendingTotals current = totals;
            Component message = Component.translatable("mending-tracker.hud",
                    Component.literal(Integer.toString(current.damagedItems())).withStyle(ChatFormatting.GREEN),
                    Component.literal(Long.toString(current.xpRequired())).withStyle(ChatFormatting.GREEN));
            var options = renderOptions();
            int messageWidth = client.font.width(message);
            int x = options.xPos * graphics.guiWidth() / 100;
            int y = options.yPos * graphics.guiHeight() / 100;
            int left = switch (options.textAlignment) {
                case Left -> x;
                case Center -> x - messageWidth / 2 - 1;
                case Right -> x - messageWidth - 3;
            };
            graphics.fill(left, y, left + messageWidth + 3, y + client.font.lineHeight + 3, 0x60000000);
            graphics.text(client.font, message, left + 2, y + 2, 0xFFFFFFFF);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("mendingTracker").executes(context -> {
                    client.execute(() -> client.gui.setScreen(new ConfigScreen(null)));
                    return 1;
                })));
        LOGGER.info("Mending Tracker initialized for Minecraft 26.3");
    }
}
