package me.rof_loyaltyrewards.events;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.gui.RewardGUI;
import me.rof_loyaltyrewards.handle.RewardEntry;
import me.rof_loyaltyrewards.handle.rewardHandler;
import me.rof_loyaltyrewards.tasks.OnlineRewardScheduler;
import me.rof_loyaltyrewards.utils.OnlineSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinEventListener implements Listener {
    private final ROF_LoyaltyRewards plugin;
    private final rewardHandler rewardHandler;
    private final RewardGUI rewardGUI;
    private final OnlineSessionManager sessionManager;
    private final OnlineRewardScheduler onlineRewardScheduler;

    public PlayerJoinEventListener(ROF_LoyaltyRewards plugin, rewardHandler rewardHandler, RewardGUI rewardGUI,
                                    OnlineSessionManager sessionManager, OnlineRewardScheduler onlineRewardScheduler) {
        this.plugin = plugin;
        this.rewardHandler = rewardHandler;
        this.rewardGUI = rewardGUI;
        this.sessionManager = sessionManager;
        this.onlineRewardScheduler = onlineRewardScheduler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Start tracking real elapsed online time for this session, and arm the
        // next online reward so it fires the instant it's earned.
        sessionManager.startSession(player.getUniqueId());
        onlineRewardScheduler.scheduleNext(player);

        if (player.hasPermission("rofow.exempt")) return;

        // Login rewards (daily/weekly/monthly/custom) grant instantly on join -
        // no elapsed-time wait, just "has enough time passed since last claim".
        rewardHandler.handleLoginRewards(player);

        List<RewardEntry> eligible = rewardHandler.getEligibleOfflineRewards(player);
        if (eligible.isEmpty()) return;

        if (plugin.getConfig().getBoolean("gui.auto-open-on-join", true)) {
            // Open a tick later so the player's inventory view is ready right after join.
            Bukkit.getScheduler().runTaskLater(plugin, () -> rewardGUI.open(player), 20L);
        } else {
            // GUI auto-open disabled: grant everything automatically, no GUI.
            for (RewardEntry entry : eligible) {
                rewardHandler.grant(player, entry);
            }
        }
    }
}
