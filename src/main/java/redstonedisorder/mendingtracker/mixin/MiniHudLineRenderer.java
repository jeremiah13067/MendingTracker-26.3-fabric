package redstonedisorder.mendingtracker.mixin;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.config.InfoToggle;
import fi.dy.masa.minihud.event.RenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstonedisorder.mendingtracker.MendingTracker;

@Mixin(value = RenderHandler.class, remap = false)
public abstract class MiniHudLineRenderer {
    @Shadow public abstract void addLine(String text);

    @Inject(method = "addLine(Lfi/dy/masa/minihud/config/InfoToggle;)V", at = @At("TAIL"))
    private void addMendingLine(InfoToggle type, CallbackInfo ci) {
        if (type.name().equals("MENDING_INFO")) {
            var totals = MendingTracker.totals();
            addLine(StringUtils.translate("minihud.info_line.mending_info",
                    GuiBase.TXT_GREEN, totals.damagedItems(), GuiBase.TXT_RST, GuiBase.TXT_GREEN, totals.xpRequired()));
        }
    }
}