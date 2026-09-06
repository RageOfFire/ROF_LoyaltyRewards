package me.rof_loyaltyrewards.handle;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.utils.ClaimTracker;
import me.rof_loyaltyrewards.utils.FileLog;
import me.rof_loyaltyrewards.utils.OnlineSessionManager;
import me.rof_loyaltyrewards.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;

public class rewardHandler {
    private final ROF_LoyaltyRewards plugin;
    private final FileLog fileLog;
    private final ClaimTracker claimTracker;
    private final OnlineSessionManager sessionManager;

    public rewardHandler(ROF_LoyaltyRewards plugin, OnlineSessionManager sessionManager) {
        this.plugin = plugin;
        this.fileLog = new FileLog(plugin);
        this.claimTracker = new ClaimTracker(plugin);
        this.sessionManager = sessionManager;
    }

    public ClaimTracker getClaimTracker() {
        return claimTracker;
    }

    /** Reads a "rewards" or "online-rewards" config section into RewardEntry objects. */
    public List<RewardEntry> loadEntries(String sectionPath, String type) {
        List<RewardEntry> entries = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionPath);
        if (section == null) return entries;

        for (String id : section.getKeys(false)) {
            String base = sectionPath + "." + id;
            String permission = plugin.getConfig().getString(base + ".permission");
            String timeString = plugin.getConfig().getString(base + ".time");
            long timeMillis = TimeConverter.convertToMilliseconds(timeString, plugin.getLogger(), sectionPath + "." + id);
            String message = plugin.getConfig().getString(base + ".message");
            String execute = plugin.getConfig().getString(base + ".execute");
            List<String> commands = plugin.getConfig().getStringList(base + ".commands");
            entries.add(new RewardEntry(id, type, permission, timeString, timeMillis, message, execute, commands));
        }
        return entries;
    }

    /** Total offline duration in millis for a player, based on last-played timestamp. */
    public long getOfflineMillis(Player player) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(player.getUniqueId());
        return System.currentTimeMillis() - offlinePlayer.getLastPlayed();
    }

    /**
     * Elapsed real (wall-clock) time in the player's current online session,
     * in milliseconds. Used instead of the PLAY_ONE_MINUTE statistic so
     * online rewards fire at the real moment they're earned, not delayed or
     * skewed by tick drift.
     */
    public long getOnlineMillis(Player player) {
        return sessionManager.getSessionMillis(player.getUniqueId());
    }

    public boolean isEligible(Player player, RewardEntry entry, long elapsedMillis) {
        if (player.hasPermission("rofow.exempt")) return false;
        if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) return false;
        if (elapsedMillis < entry.getTimeMillis()) return false;
        return claimTracker.canClaim(player.getUniqueId(), entry.getType() + ":" + entry.getId(), entry.getTimeMillis());
    }

    /** Runs the commands/message/logging for a reward and marks it claimed. Call only after isEligible(). */
    public void grant(Player player, RewardEntry entry) {
        String timeString = entry.getTimeString();
        String alertMessage = plugin.getConfig().getString("messages.alert");
        if (alertMessage != null) {
            alertMessage = alertMessage
                    .replace("%player%", player.getName())
                    .replace("%time%", timeString == null ? "" : timeString)
                    .replace("%reward%", entry.getId());

            if (plugin.getConfig().getBoolean("logs.console")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', alertMessage));
            }
            if (plugin.getConfig().getBoolean("logs.player")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("rofow.alert")) {
                        online.sendMessage(ChatColor.translateAlternateColorCodes('&', alertMessage));
                    }
                }
            }
            if (plugin.getConfig().getBoolean("logs.file")) {
                fileLog.LogToFile(alertMessage);
            }
        }

        for (String command : entry.getCommands()) {
            String formattedCommand = command.replace("%player%", player.getName());
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                formattedCommand = PlaceholderAPI.setPlaceholders(player, formattedCommand);
            }
            if ("CONSOLE".equalsIgnoreCase(entry.getExecute())) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), formattedCommand);
            } else if ("PLAYER".equalsIgnoreCase(entry.getExecute())) {
                plugin.getServer().dispatchCommand(player, formattedCommand);
            }
        }

        String message = entry.getMessage();
        if (message != null && !message.isEmpty()) {
            String formattedMessage = message.replace("%player%", player.getName());
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        }

        claimTracker.markClaimed(player.getUniqueId(), entry.getType() + ":" + entry.getId());
    }

    /** Legacy entry point: auto-checks and grants all eligible OFFLINE rewards immediately (no GUI). */
    public void handleRewards(Player player) {
        if (player.hasPermission("rofow.exempt")) return;
        long offlineMillis = getOfflineMillis(player);
        for (RewardEntry entry : loadEntries("rewards", "offline")) {
            if (isEligible(player, entry, offlineMillis)) {
                grant(player, entry);
            }
        }
    }

    /**
     * Checks and instantly grants "login-reward" entries on join. Unlike
     * offline rewards, eligibility here isn't based on how long the player
     * was away - it's purely "has enough real time passed since I last
     * claimed this reward" (using each entry's own "time" as the interval),
     * so a single section covers daily, weekly, monthly, or any custom
     * cadence just by varying the "time" value per entry.
     */
    public void handleLoginRewards(Player player) {
        if (player.hasPermission("rofow.exempt")) return;
        for (RewardEntry entry : loadEntries("login-reward", "login")) {
            String key = entry.getType() + ":" + entry.getId();
            if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) continue;
            if (claimTracker.canClaim(player.getUniqueId(), key, entry.getTimeMillis())) {
                grant(player, entry);
            }
        }
    }

    /** Returns the offline rewards a player currently qualifies for but hasn't claimed yet. */
    public List<RewardEntry> getEligibleOfflineRewards(Player player) {
        List<RewardEntry> eligible = new ArrayList<>();
        long offlineMillis = getOfflineMillis(player);
        for (RewardEntry entry : loadEntries("rewards", "offline")) {
            if (isEligible(player, entry, offlineMillis)) {
                eligible.add(entry);
            }
        }
        return eligible;
    }

    /** Builds the full status list (claimable / locked / cooldown / no-permission) for one reward section, for the GUI. */
    public List<RewardStatus> getStatuses(Player player, String sectionPath, String type) {
        List<RewardStatus> statuses = new ArrayList<>();
        long elapsed;
        if ("offline".equals(type)) {
            elapsed = getOfflineMillis(player);
        } else if ("online".equals(type)) {
            elapsed = getOnlineMillis(player);
        } else {
            // "login" rewards have no elapsed-time gate - they're granted the
            // instant their cooldown clears, so always treat the "time reached"
            // condition as satisfied here.
            elapsed = Long.MAX_VALUE;
        }

        for (RewardEntry entry : loadEntries(sectionPath, type)) {
            if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) {
                statuses.add(new RewardStatus(entry, RewardStatus.State.NO_PERMISSION, 0));
                continue;
            }

            String key = entry.getType() + ":" + entry.getId();
            boolean canClaim = claimTracker.canClaim(player.getUniqueId(), key, entry.getTimeMillis());

            if (!canClaim) {
                long last = claimTracker.getLastClaimed(player.getUniqueId(), key);
                long remaining = Math.max(0, entry.getTimeMillis() - (System.currentTimeMillis() - last));
                statuses.add(new RewardStatus(entry, RewardStatus.State.COOLDOWN, remaining));
            } else if (elapsed >= entry.getTimeMillis()) {
                statuses.add(new RewardStatus(entry, RewardStatus.State.CLAIMABLE, 0));
            } else {
                statuses.add(new RewardStatus(entry, RewardStatus.State.LOCKED, entry.getTimeMillis() - elapsed));
            }
        }
        return statuses;
    }
}
