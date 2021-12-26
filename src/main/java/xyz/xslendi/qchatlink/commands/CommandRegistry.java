package xyz.xslendi.qchatlink.commands;

import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import xyz.xslendi.qchatlink.Settings;
import xyz.xslendi.qchatlink.qchatlink;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("qcl")
                    .requires(Permissions.require("qchatlink.main", true))
                    .executes(CommandRegistry::about)

                    .then(literal("reload")
                            .requires(Permissions.require("qchatlink.reload", true))
                            .executes(context -> {
                                Settings.load();
                                context.getSource().sendFeedback(new LiteralText("Reloaded config!").formatted(Formatting.GREEN), false);
                                return 1;
                            }))
            );
        });
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText(
                        "§eQChatLink§r: §2v" + qchatlink.VERSION + "\n§6Made by§f: §2" + qchatlink.AUTHORS
                ), false);

        return 1;
    }
}
