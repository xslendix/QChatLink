package xyz.xslendi.qchatlink;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import xyz.xslendi.qchatlink.commands.CommandRegistry;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class qchatlink implements ModInitializer {

    public static String VERSION = FabricLoader.getInstance().getModContainer("qchatlink").get().getMetadata().getVersion().getFriendlyString();
    public static String AUTHORS = FabricLoader.getInstance().getModContainer("qchatlink").get().getMetadata().getAuthors().stream().map(Person::getName).collect(Collectors.joining(", "));

    public static String runDirectory;
    private static HashMap<String, String> mentionables;

    public static JDA jda;

    @Override
    public void onInitialize() {

        Settings.load();

        CommandRegistry.register();

        JDABuilder builder = JDABuilder.createDefault(Settings.Discord.TOKEN);

        Bot bot = new Bot();

        builder.addEventListeners(bot);

        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.playing("Minecraft"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);

        try {
            jda = builder.build();
            jda.awaitReady();

            mentionables = buildDiscordMentionables();

            ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer server) -> {
                var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
                channel.sendMessage(Settings.Messages.Discord.SERVER_STARTING).queue();
            });
            ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
                var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
                channel.sendMessage(Settings.Messages.Discord.SERVER_START).queue();
            });

            ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer server) -> {
                var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
                channel.sendMessage(Settings.Messages.Discord.SERVER_STOPPING).queue();
            });
            ServerLifecycleEvents.SERVER_STOPPED.register((MinecraftServer server) -> {
                var channel = jda.getTextChannelById(Settings.Discord.CHANNEL_ID);
                channel.sendMessage(Settings.Messages.Discord.SERVER_STOP).queue();
                jda.shutdown();
            });
        } catch (Exception e) {
            if (jda != null)
                jda.shutdownNow();
            e.printStackTrace();
        }

    }

    private static HashMap<String, String> buildDiscordMentionables() {
        Guild g = jda.getTextChannelById(Settings.Discord.CHANNEL_ID).getGuild();
        HashMap<String, String> mentionables = new HashMap<>();
        for (net.dv8tion.jda.api.entities.Member member : g.loadMembers().get()) {
            User u = member.getUser();
            mentionables.put(u.getName().toLowerCase(), u.getId());
        }
        return mentionables;
    }

    private static String findMention(String message) {
        ArrayList<String> words = new ArrayList<>();
        Collections.addAll(words, message.split(" "));
        StringBuilder builder = new StringBuilder();
        for (String s : words) {
            if (!s.contains("@")) {
                builder.append(s);
            } else {
                String string = s.replace("@", "");
                User user = null;
                if (mentionables.get(string.toLowerCase()) != null) {
                    user = jda.retrieveUserById(mentionables.get(string.toLowerCase())).complete();
                }
                if (user != null) {
                    builder.append(user.getAsMention());
                } else {
                    builder.append(string);
                }
            }
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    public static void OnMessage(ServerPlayerEntity player, String msg) {
        msg = findMention(msg);
        String data = "{ \"username\": \"" + player.getName().asString() +
                "\", \"content\": \"" + msg +
                "\", \"avatar_url\": \"" + "https://crafatar.com/avatars/" + player.getUuid() +
                "\"}";

        new Thread(() -> {
            HttpClient httpClient = HttpClientBuilder.create().build();
            try {
                StringEntity params = new StringEntity(data, ContentType.APPLICATION_JSON);
                HttpPost request = new HttpPost(Settings.Discord.WEBHOOK_URL);
                request.setEntity(params);
                httpClient.execute(request);
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }).start();
    }

}
