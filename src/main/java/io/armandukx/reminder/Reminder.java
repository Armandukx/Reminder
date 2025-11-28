package io.armandukx.reminder;

import io.armandukx.reminder.Handlers.Command;
import io.armandukx.reminder.Handlers.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Reminder implements ClientModInitializer {
    private boolean RemindedServer = false;
    private int PingTicksLeft = 0;
    private int PingsRemaining = 0;
    private final int TICKS_BETWEEN_PINGS = 2;

    @Override
    public void onInitializeClient() {
        Config.load(FabricLoader.getInstance().getConfigDir().toFile());
        Command.register();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ServerInfo serverData = client.getCurrentServerEntry();
            if (serverData == null || client.player == null) {
                RemindedServer = false;
                return;
            }

            String hostname = serverData.address;

            if (!RemindedServer && Config.data.reminderEnabled && Config.data.serverReminders.keySet().stream().anyMatch(hostname::contains)) {

                RemindedServer = true;

                SendChat(Config.data.messages.prefix + Config.data.messages.onJoin, false);

                PingsRemaining = 3;
                PingTicksLeft = 0;

                String[] lines = Config.data.serverReminders.getOrDefault(hostname, new String[]{});
                for (String line : lines) {
                    SendChat(Config.data.messages.prefix + line, true);
                }
            }

            if (PingsRemaining > 0) {
                if (PingTicksLeft <= 0) {
                    RegistryEntry.Reference<net.minecraft.sound.SoundEvent> soundRef = SoundEvents.BLOCK_NOTE_BLOCK_PLING;
                    client.player.playSoundToPlayer(soundRef.value(), SoundCategory.MASTER, 2.0f, 1.0f);
                    PingsRemaining--;
                    PingTicksLeft = TICKS_BETWEEN_PINGS;
                } else {
                    PingTicksLeft--;
                }
            }
        });
    }

    private void SendChat(String text, boolean bold) {
        MutableText message = Text.literal(text.replace("&", "§"));
        if (bold) message = message.styled(style -> style.withBold(true));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(message, false);
    }
}