package dev.senzore.logrecord.mixin;

import dev.senzore.logrecord.LogRecordClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void logrecord$captureText(DeltaTracker delta, boolean renderLevel, boolean renderScreen, CallbackInfo ci) {
        LogRecordClient.captureFrame(guiRenderState);
    }
}
