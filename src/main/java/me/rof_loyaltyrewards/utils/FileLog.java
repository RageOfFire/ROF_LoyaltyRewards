package me.rof_loyaltyrewards.utils;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLog {
    private final ROF_LoyaltyRewards plugin;

    public FileLog(ROF_LoyaltyRewards plugin) {
        this.plugin = plugin;
    }

    /**
     * Writes a line to log.txt. Runs off the main thread so file I/O never
     * causes a tick hitch on player join.
     */
    public void LogToFile(String message) {
        String stripedMessage = ChatColor.stripColor(message);
        SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = logDateFormat.format(new Date());
        String logMessage = "[" + timestamp + "] " + stripedMessage + "\n";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File logFile = new File(plugin.getDataFolder(), "log.txt");
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logMessage);
                writer.flush();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not write to log.txt: " + e.getMessage());
            }
        });
    }
}
