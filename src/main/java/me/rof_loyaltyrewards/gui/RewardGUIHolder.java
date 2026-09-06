package me.rof_loyaltyrewards.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/** Marks an inventory as the reward GUI, tracking which slots hold a claimable offline reward. */
public class RewardGUIHolder implements InventoryHolder {
    private Inventory inventory;
    private final Map<Integer, String> claimableSlots = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /** Marks a slot as clickable-to-claim for the given (offline) reward id. */
    public void putClaimable(int slot, String rewardId) {
        claimableSlots.put(slot, rewardId);
    }

    /** Returns the reward id claimable at this slot, or null if the slot is informational only. */
    public String getClaimableRewardId(int slot) {
        return claimableSlots.get(slot);
    }
}
