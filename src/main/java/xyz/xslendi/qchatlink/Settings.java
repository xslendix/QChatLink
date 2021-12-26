package xyz.xslendi.qchatlink;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class Settings {

    public static class Discord {
        public static String WEBHOOK_URL;
        public static String TOKEN;
        public static String CHANNEL_ID;

        public static String format(String input) {
            return input
                    .replace("%webhook%", WEBHOOK_URL)
                    .replace("%token%", TOKEN)
                    .replace("%channel_id%", CHANNEL_ID);
        }
    }

    public static class Messages {
        public static String PREFIX, SUFFIX;

        public static class InGame {
            public static String MESSAGE;
            public static String MESSAGE_PINNED;

            public static String NEW_THREAD;

            public static String MEMBER_JOIN;
            public static String MEMBER_LEAVE;
            public static String MEMBER_BOOST;
        }

        public static class Discord {
            public static String SERVER_STARTING;
            public static String SERVER_START;

            public static String SERVER_STOPPING;
            public static String SERVER_STOP;

            public static String JOIN_MESSAGE;
            public static String LEAVE_MESSAGE;
        }

        public static String format(String input) {
            return input
                    .replace("%prefix%", PREFIX)
                    .replace("%suffix%", SUFFIX);
        }
    }

    public static void load() {
        var yaml = new Yaml();
        var file = new File("config/qchatlink.yaml");

        if (!file.exists()) {
            try {
                Files.copy(qchatlink.class.getResourceAsStream("/qchatlink.yaml"), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            var filestream = new FileInputStream(file);

            Map<String, Object> obj = yaml.load(filestream);

            Discord.WEBHOOK_URL              = (String) obj.get("webhook");
            Discord.TOKEN                    = (String) obj.get("token");
            Discord.CHANNEL_ID               = (String) obj.get("channel_id");

            Messages.PREFIX                  = (String) obj.get("prefix");
            Messages.SUFFIX                  = (String) obj.get("suffix");

            Messages.InGame.MESSAGE          = (String) obj.get("message");
            Messages.InGame.MESSAGE_PINNED   = (String) obj.get("message_pinned");
            Messages.InGame.NEW_THREAD       = (String) obj.get("new_thread");

            Messages.InGame.MEMBER_JOIN      = (String) obj.get("member_join");
            Messages.InGame.MEMBER_LEAVE     = (String) obj.get("member_leave");
            Messages.InGame.MEMBER_BOOST     = (String) obj.get("member_boost");

            Messages.Discord.SERVER_STARTING = (String) obj.get("server_starting");
            Messages.Discord.SERVER_START    = (String) obj.get("server_start");
            Messages.Discord.SERVER_STOPPING = (String) obj.get("server_stopping");
            Messages.Discord.SERVER_STOP     = (String) obj.get("server_stop");

            Messages.Discord.JOIN_MESSAGE    = (String) obj.get("join_message");
            Messages.Discord.LEAVE_MESSAGE   = (String) obj.get("leave_message");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
