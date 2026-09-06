package me.rof_loyaltyrewards.tasks;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.handle.RewardEntry;
import me.rof_loyaltyrewards.handle.rewardHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instead of polling all online players every N seconds, this schedules one
 * delayed task per player timed to fire exactly when their next unclaimed
 * online reward becomes eligible - giving the same "grants the instant it's
 * earned" behaviour offline rewards already have on join.
 */
public class OnlineRewardScheduler {
    private final ROF_LoyaltyRewards plugin;
    private final rewardHandler rewardHandler;
    private final Map<UUID, BukkitTask> scheduled = new ConcurrentHashMap<>();

    public OnlineRewardScheduler(ROF_LoyaltyRewards plugin, rewardHandler rewardHandler) {
        this.plugin = plugin;
        this.rewardHandler = rewardHandler;
    }

    /** Call when a player joins (or the plugin reloads while they're online) to (re)arm their next reward. */
    public void scheduleNext(Player player) {
        cancel(player.getUniqueId());
        if (!plugin.getConfig().getBoolean("online-rewards-enabled", true)) return;
        if (player.hasPermission("rofow.exempt")) return;

        long elapsed = rewardHandler.getOnlineMillis(player);
        RewardEntry next = findNextUnclaimed(player, elapsed);
        if (next == null) return;

        long delayMillis = Math.max(0, next.getTimeMillis() - elapsed);
        long delayTicks = Math.max(1L, delayMillis / 50L);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            long nowElapsed = rewardHandler.getOnlineMillis(player);
            if (rewardHandler.isEligible(player, next, nowElapsed)) {
                rewardHandler.grant(player, next);
            }
            // Arm whatever the next soonest unclaimed reward is now.
            scheduleNext(player);
        }, delayTicks);

        scheduled.put(player.getUniqueId(), task);
    }

    public void cancel(UUID uuid) {
        BukkitTask task = scheduled.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAll() {
        for (BukkitTask task : scheduled.values()) {
            task.cancel();
        }
        scheduled.clear();
    }

    private RewardEntry findNextUnclaimed(Player player, long elapsed) {
        RewardEntry next = null;
        for (RewardEntry entry : rewardHandler.loadEntries("online-rewards", "online")) {
            if (player.hasPermission("rofow.exempt")) continue;
            if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) continue;

            boolean onCooldown = !rewardHandler.getClaimTracker()
                    .canClaim(player.getUniqueId(), entry.getType() + ":" + entry.getId(), entry.getTimeMillis());
            if (onCooldown) continue;

            if (next == null || entry.getTimeMillis() < next.getTimeMillis()) {
                next = entry;
            }
        }
        return next;
    }
}
