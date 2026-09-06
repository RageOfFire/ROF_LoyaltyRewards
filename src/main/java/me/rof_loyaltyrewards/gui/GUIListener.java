package me.rof_loyaltyrewards.gui;

import me.rof_loyaltyrewards.handle.RewardEntry;
import me.rof_loyaltyrewards.handle.rewardHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {
    private final rewardHandler rewardHandler;
    private final RewardGUI rewardGUI;

    public GUIListener(rewardHandler rewardHandler, RewardGUI rewardGUI) {
        this.rewardHandler = rewardHandler;
        this.rewardGUI = rewardGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof RewardGUIHolder)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        RewardGUIHolder rewardHolder = (RewardGUIHolder) holder;
        String rewardId = rewardHolder.getClaimableRewardId(event.getSlot());
        if (rewardId == null) return; // Informational slot, nothing to claim.

        Player player = (Player) event.getWhoClicked();

        RewardEntry entry = null;
        for (RewardEntry candidate : rewardHandler.loadEntries("rewards", "offline")) {
            if (candidate.getId().equals(rewardId)) {
                entry = candidate;
                break;
            }
        }
        if (entry == null) return;

        long offlineMillis = rewardHandler.getOfflineMillis(player);
        if (!rewardHandler.isEligible(player, entry, offlineMillis)) {
            player.sendMessage("This reward is no longer available.");
            player.closeInventory();
            return;
        }

        rewardHandler.grant(player, entry);
        // Refresh the GUI in place so its status (now claimed) is accurate.
        rewardGUI.open(player);
    }
}
