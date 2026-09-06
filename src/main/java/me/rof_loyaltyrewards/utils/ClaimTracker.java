package me.rof_loyaltyrewards.utils;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Tracks the last time each player claimed each reward (offline or online),
 * so a reward is not handed out again until its own time requirement has
 * elapsed a second time.
 *
 * Data is stored in claims.yml as:
 * <uuid>:
 *   <rewardId>: <epoch millis of last claim>
 */
public class ClaimTracker {
    private final ROF_LoyaltyRewards plugin;
    private final File file;
    private FileConfiguration data;

    public ClaimTracker(ROF_LoyaltyRewards plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "claims.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create claims.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public long getLastClaimed(UUID uuid, String rewardId) {
        return data.getLong(uuid.toString() + "." + rewardId, 0L);
    }

    public boolean canClaim(UUID uuid, String rewardId, long requiredIntervalMillis) {
        long last = getLastClaimed(uuid, rewardId);
        if (last == 0L) return true;
        return (System.currentTimeMillis() - last) >= requiredIntervalMillis;
    }

    public void markClaimed(UUID uuid, String rewardId) {
        data.set(uuid.toString() + "." + rewardId, System.currentTimeMillis());
        save();
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save claims.yml: " + e.getMessage());
        }
    }
}
