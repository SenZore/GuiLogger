package dev.senzore.logrecord.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
    @Accessor("leftPos") int logrecord$left();
    @Accessor("topPos") int logrecord$top();
    @Accessor("imageWidth") int logrecord$width();
    @Accessor("imageHeight") int logrecord$height();
}
