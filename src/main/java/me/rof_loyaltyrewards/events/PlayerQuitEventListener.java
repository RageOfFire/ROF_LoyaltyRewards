package me.rof_loyaltyrewards.events;

import me.rof_loyaltyrewards.tasks.OnlineRewardScheduler;
import me.rof_loyaltyrewards.utils.OnlineSessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {
    private final OnlineSessionManager sessionManager;
    private final OnlineRewardScheduler onlineRewardScheduler;

    public PlayerQuitEventListener(OnlineSessionManager sessionManager, OnlineRewardScheduler onlineRewardScheduler) {
        this.sessionManager = sessionManager;
        this.onlineRewardScheduler = onlineRewardScheduler;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        onlineRewardScheduler.cancel(player.getUniqueId());
        sessionManager.endSession(player.getUniqueId());
    }
}
