package io.armandukx.reminder.Handlers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.armandukx.reminder.Reminder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;

public class Command {
    public static ServerCommandSource source;
    public static void register() {
        source = new Reminder.ClientSource();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("reminder")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String server = getCurrentServerIP();
                                            if (server == null) {
                                                SendPlayerMessage("Not connected to any server!");
                                                return 0;
                                            }

                                            String message = StringArgumentType.getString(ctx, "message");
                                            String[] old = Config.data.serverReminders.getOrDefault(server, new String[]{});
                                            String[] newLines = Arrays.copyOf(old, old.length + 1);
                                            newLines[old.length] = message;
                                            Config.data.serverReminders.put(server, newLines);
                                            Config.save();

                                            SendPlayerMessage("Added message for " + server);
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("edit")
                                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String server = getCurrentServerIP();
                                            if (server == null) {
                                                SendPlayerMessage("Not connected to any server!");
                                                return 0;
                                            }

                                            String message = StringArgumentType.getString(ctx, "message");
                                            Config.data.serverReminders.put(server, message.split("\n"));
                                            Config.save();

                                            SendPlayerMessage("Updated messages for " + server);
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("delete")
                                .executes(ctx -> {
                                    String server = getCurrentServerIP();
                                    if (server == null) {
                                        SendPlayerMessage("Not connected to any server!");
                                        return 0;
                                    }

                                    Config.data.serverReminders.remove(server);
                                    Config.save();

                                    SendPlayerMessage("Removed messages for " + server);
                                    return 1;
                                })
                        )
        );
    }

    private static String getCurrentServerIP() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return null;
    }

    private static void SendPlayerMessage(String message) {
        source.sendMessage(Text.literal("[Reminder] " + message));
    }
}