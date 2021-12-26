package xyz.xslendi.qchatlink;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static net.dv8tion.jda.api.entities.MessageType.*;
import static xyz.xslendi.qchatlink.qchatlink.jda;

public class Bot extends ListenerAdapter {

    private PlayerManager pm;

    public Bot() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> pm = server.getPlayerManager());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();

        if (msg.getType() == GUILD_MEMBER_BOOST || msg.getType() == GUILD_MEMBER_JOIN) {
            if (pm != null) {
                sendMessage(switch (msg.getType()) {
                    case GUILD_MEMBER_BOOST -> new LiteralText(
                            messageFormat(event, Settings.Messages.format(Settings.Messages.InGame.MEMBER_BOOST))
                            // "§e" + msg.getAuthor().getAsTag() + "§d has boosted the server!"
                    );
                    case GUILD_MEMBER_JOIN -> new LiteralText(
                            messageFormat(event, Settings.Messages.format(Settings.Messages.InGame.MEMBER_JOIN))
                            // "§e" + msg.getAuthor().getAsTag() + "§d has joined the server!"
                    );
                    default -> new LiteralText("");
                });
            }
        } else if (msg.getChannel().getId().equals(Settings.Discord.CHANNEL_ID) &&
                msg.getAuthor() != msg.getJDA().getSelfUser() &&
                !(msg.isWebhookMessage()) &&
                !(msg.getAuthor().isBot()) &&
                pm != null
            ) {

            Text text = switch (msg.getType()) {
                case DEFAULT -> new LiteralText(
                        messageFormat(event, Settings.Messages.format(Settings.Messages.InGame.MESSAGE))
                        //"§e" + msg.getAuthor().getAsTag() + "§r: " + msg.getContentRaw()
                );
                case THREAD_CREATED -> new LiteralText(
                        messageFormat(event, Settings.Messages.format(Settings.Messages.InGame.NEW_THREAD))
                        // "§e" + msg.getAuthor().getAsTag() + "§d has created a new thread: §r" + msg.getContentDisplay()
                );
                case CHANNEL_PINNED_ADD -> new LiteralText(
                        messageFormat(event, Settings.Messages.format(Settings.Messages.InGame.MESSAGE_PINNED))
                        // "§e" + msg.getAuthor().getAsTag() + "§d has pinned a message."
                );
                default -> new LiteralText("");
            };

            if (msg.getContentRaw().length() > 0)
                sendMessage(text);
        }

    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (event.getGuild() == jda.getTextChannelById(Settings.Discord.CHANNEL_ID).getGuild()) {
            sendMessage(new LiteralText(
                    guildFormat(event, Settings.Messages.format(Settings.Messages.InGame.MEMBER_LEAVE))
                    // "§e" + event.getUser().getAsTag() +
                    // "§d has left the server!"
            ));
        }
    }

    private void sendMessage(Text text) {
        // text = new LiteralText("§8[§9Discord§8] ").append(text);
        if (pm != null && text.asString().length() > 0) {
            // Text finalText = text;
            pm.getPlayerList().forEach(
                    serverPlayerEntity -> serverPlayerEntity.sendMessage(text, false)
            );
        }
    }

    private String guildFormat(GuildMemberRemoveEvent event, String input) {
        return input
                .replace("%tag%", event.getUser().getAsTag())
                .replace("%name%", event.getUser().getName());
    }

    private String messageFormat(MessageReceivedEvent event, String input) {
        return input
                .replace("%tag%", event.getAuthor().getAsTag())
                .replace("%name%", event.getAuthor().getName())
                .replace("%message%", event.getMessage().getContentRaw())
                .replace("%title%", event.getMessage().getContentStripped());
    }

}
