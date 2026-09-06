package me.rof_loyaltyrewards.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.handle.RewardEntry;
import me.rof_loyaltyrewards.handle.rewardHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Exposes ROF_LoyaltyRewards data through PlaceholderAPI.
 *
 * %rofloyaltyrewards_offline_time%       - formatted current offline duration
 * %rofloyaltyrewards_online_time%        - formatted current online playtime
 * %rofloyaltyrewards_eligible_offline%   - count of unclaimed offline rewards player qualifies for
 * %rofloyaltyrewards_eligible_online%    - count of unclaimed online rewards player qualifies for
 * %rofloyaltyrewards_next_offline%       - id of the next offline reward not yet claimed
 * %rofloyaltyrewards_next_offline_time%  - time requirement of that next reward
 */
public class RewardPlaceholderExpansion extends PlaceholderExpansion {
    private final ROF_LoyaltyRewards plugin;
    private final rewardHandler rewardHandler;

    public RewardPlaceholderExpansion(ROF_LoyaltyRewards plugin, rewardHandler rewardHandler) {
        this.plugin = plugin;
        this.rewardHandler = rewardHandler;
    }

    @Override
    public String getIdentifier() {
        return "rofloyaltyrewards";
    }

    @Override
    public String getAuthor() {
        return "RageOfFire";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) return "";

        if (params.equalsIgnoreCase("offline_time")) {
            long millis = System.currentTimeMillis() - offlinePlayer.getLastPlayed();
            return formatDuration(millis);
        }

        if (!offlinePlayer.isOnline() || !(offlinePlayer.getPlayer() instanceof Player)) {
            // The remaining placeholders need a live Player (permissions, statistics).
            return "";
        }
        Player player = offlinePlayer.getPlayer();

        switch (params.toLowerCase()) {
            case "online_time":
                return formatDuration(rewardHandler.getOnlineMillis(player));

            case "eligible_offline":
                return String.valueOf(rewardHandler.getEligibleOfflineRewards(player).size());

            case "eligible_online": {
                long onlineMillis = rewardHandler.getOnlineMillis(player);
                int count = 0;
                for (RewardEntry entry : rewardHandler.loadEntries("online-rewards", "online")) {
                    if (rewardHandler.isEligible(player, entry, onlineMillis)) count++;
                }
                return String.valueOf(count);
            }

            case "next_offline":
            case "next_offline_time": {
                RewardEntry next = findNextUnclaimed(player, "rewards", "offline");
                if (next == null) return params.equalsIgnoreCase("next_offline") ? "none" : "";
                return params.equalsIgnoreCase("next_offline") ? next.getId() : next.getTimeString();
            }

            default:
                return null;
        }
    }

    private RewardEntry findNextUnclaimed(Player player, String section, String type) {
        List<RewardEntry> entries = rewardHandler.loadEntries(section, type);
        RewardEntry next = null;
        for (RewardEntry entry : entries) {
            if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) continue;
            long lastClaimed = rewardHandler.getClaimTracker().getLastClaimed(player.getUniqueId(), type + ":" + entry.getId());
            if (lastClaimed != 0L && !rewardHandler.getClaimTracker().canClaim(player.getUniqueId(), type + ":" + entry.getId(), entry.getTimeMillis())) {
                continue;
            }
            if (next == null || entry.getTimeMillis() < next.getTimeMillis()) {
                next = entry;
            }
        }
        return next;
    }

    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");
        return sb.toString();
    }
}
