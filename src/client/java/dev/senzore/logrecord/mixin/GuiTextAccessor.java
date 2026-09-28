package dev.senzore.logrecord.mixin;

import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextRenderState.class)
public interface GuiTextAccessor {
    @Accessor("text") FormattedCharSequence logrecord$text();
    @Accessor("x") int logrecord$x();
    @Accessor("y") int logrecord$y();
    @Accessor("color") int logrecord$color();
}
