package io.armandukx.reminder.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    public static ReminderConfig data;

    public static void load(File configFolder) {
        File armandFolder = new File(configFolder, "Armandukx");
        if (!armandFolder.exists()) armandFolder.mkdirs();

        file = new File(armandFolder, "Reminder.json");

        try {
            if (!file.exists()) {
                data = new ReminderConfig();
                save();
            } else {
                FileReader reader = new FileReader(file);
                data = gson.fromJson(reader, ReminderConfig.class);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            data = new ReminderConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ReminderConfig {
        public boolean reminderEnabled = true;

        public Map<String, String[]> serverReminders = new HashMap<>();

        public Messages messages = new Messages();
        public static class Messages {
            public String prefix = "§6§l[Reminder] §f";
            public String onJoin = "Here's your reminder:";
        }
    }
}