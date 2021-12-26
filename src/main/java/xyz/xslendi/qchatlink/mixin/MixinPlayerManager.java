package xyz.xslendi.qchatlink.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.xslendi.qchatlink.Settings;

import static xyz.xslendi.qchatlink.qchatlink.jda;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(at = @At("RETURN"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
        if (channel != null) {
            channel.sendMessage(
                    format(player, Settings.Messages.Discord.JOIN_MESSAGE)
            ).queue();
        }
    }

    @Inject(at = @At("RETURN"), method = "remove")
    public void remove(ServerPlayerEntity player, CallbackInfo ci) {
        var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
        if (channel != null) {
            channel.sendMessage(
                    format(player, Settings.Messages.Discord.LEAVE_MESSAGE)
            ).queue();
        }
    }

    private static String format(ServerPlayerEntity player, String input) {
        return input.replace("%name%", player.getName().asString());
    }
}
