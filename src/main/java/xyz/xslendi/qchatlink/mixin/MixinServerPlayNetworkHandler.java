package xyz.xslendi.qchatlink.mixin;

import xyz.xslendi.qchatlink.qchatlink;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.filter.TextStream.Message;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), method = "handleMessage", cancellable = true)
    private void handleMessage(Message message, CallbackInfo ci) {
        String string = message.getRaw();
        qchatlink.OnMessage(this.player, string);
    }

}
