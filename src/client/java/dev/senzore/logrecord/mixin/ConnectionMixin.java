package dev.senzore.logrecord.mixin;

import dev.senzore.logrecord.LocalCommands;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void logrecord$keepCommandsLocal(Packet<?> packet, ChannelFutureListener listener,
                                             boolean flush, CallbackInfo ci) {
        boolean blocked = switch (packet) {
            case ServerboundChatCommandPacket command -> LocalCommands.isRecorder(command.command());
            case ServerboundChatCommandSignedPacket command -> LocalCommands.isRecorder(command.command());
            case ServerboundCommandSuggestionPacket suggestion -> LocalCommands.isRecorderSuggestion(suggestion.getCommand());
            case ServerboundChatPacket chat -> chat.message().stripLeading().startsWith("/")
                    && LocalCommands.isRecorder(chat.message());
            default -> false;
        };
        if (blocked) ci.cancel();
    }
}
