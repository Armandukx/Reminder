package io.armandukx.reminder;

import io.armandukx.reminder.Handlers.Command;
import io.armandukx.reminder.Handlers.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Reminder implements ClientModInitializer {
    private boolean RemindedServer = false;

    @Override
    public void onInitializeClient() {
        Config.load(FabricLoader.getInstance().getConfigDir().toFile());
        Command.register();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ServerInfo serverData = client.getCurrentServerEntry();
            if (serverData == null) {
                RemindedServer = false;
                return;
            };

            String hostname = serverData.address;

            if (RemindedServer) return;
            RemindedServer = true;

            System.out.println("[Reminder] Connected to: " + hostname);

            if (!Config.data.reminderEnabled) return;

            boolean matches = Config.data.serverReminders.keySet().stream().anyMatch(hostname::contains);
            if (!matches) return;

            System.out.println("[Reminder] Found matching server");

            ServerCommandSource source = new ClientSource();

            sendChat(source, ConvertToText(Config.data.messages.prefix + Config.data.messages.onJoin, false));

            String[] lines = Config.data.serverReminders.getOrDefault(hostname, new String[]{});
            for (String line : lines) {
                sendChat(source, ConvertToText(Config.data.messages.prefix + line, true));
            }

            System.out.println("[Reminder] Sent reminders");
        });
    }

    private Text ConvertToText(String text, boolean bold) {
        MutableText literal = Text.literal(text.replace("&", "§"));
        if (bold) {
            literal = literal.styled(style -> style.withBold(true));
        }
        return literal;
    }

    private void sendChat(ServerCommandSource source, Text message) {
        source.sendMessage(message);
    }

    public static class ClientSource extends ServerCommandSource {
        public ClientSource() {
            super(null, null, null, null, 4, "Reminder", Text.literal("Reminder"), null, null);
        }

        @Override
        public void sendMessage(Text message) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(message, false);
            }
        }
    }
}