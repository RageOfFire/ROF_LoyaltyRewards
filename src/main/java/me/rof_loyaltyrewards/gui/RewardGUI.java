package me.rof_loyaltyrewards.gui;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.handle.RewardStatus;
import me.rof_loyaltyrewards.handle.rewardHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RewardGUI {
    private final ROF_LoyaltyRewards plugin;
    private final rewardHandler rewardHandler;

    public RewardGUI(ROF_LoyaltyRewards plugin, rewardHandler rewardHandler) {
        this.plugin = plugin;
        this.rewardHandler = rewardHandler;
    }

    /** Opens the full reward list (offline + online) for a player, with live status per entry. */
    public void open(Player player) {
        List<RewardStatus> statuses = new ArrayList<>();
        statuses.addAll(rewardHandler.getStatuses(player, "rewards", "offline"));
        statuses.addAll(rewardHandler.getStatuses(player, "online-rewards", "online"));
        statuses.addAll(rewardHandler.getStatuses(player, "login-reward", "login"));

        int size = Math.max(9, (int) (Math.ceil(statuses.size() / 9.0) * 9));
        size = Math.min(size, 54);

        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8Loyalty Rewards"));

        RewardGUIHolder holder = new RewardGUIHolder();
        Inventory inventory = plugin.getServer().createInventory(holder, size, title);
        holder.setInventory(inventory);

        int slot = 0;
        for (RewardStatus status : statuses) {
            inventory.setItem(slot, buildItem(status));
            if (status.getState() == RewardStatus.State.CLAIMABLE && "offline".equals(status.getEntry().getType())) {
                holder.putClaimable(slot, status.getEntry().getId());
            }
            slot++;
        }

        player.openInventory(inventory);
    }

    /** True if the player currently has at least one claimable offline reward - used to decide whether to auto-open on join. */
    public boolean hasClaimable(Player player) {
        for (RewardStatus status : rewardHandler.getStatuses(player, "rewards", "offline")) {
            if (status.getState() == RewardStatus.State.CLAIMABLE) return true;
        }
        return false;
    }

    private ItemStack buildItem(RewardStatus status) {
        String type = status.getEntry().getType();
        boolean isOnline = "online".equals(type);
        boolean isLogin = "login".equals(type);

        Material material;
        String nameColor;
        List<String> lore = new ArrayList<>();

        switch (status.getState()) {
            case CLAIMABLE:
                material = isOnline ? Material.CLOCK : (isLogin ? Material.SUNFLOWER : Material.CHEST);
                nameColor = "&a";
                if (!isLogin) {
                    lore.add(ChatColor.GRAY + "Required time: " + ChatColor.WHITE + status.getEntry().getTimeString());
                    lore.add("");
                }
                if (isOnline) {
                    lore.add(ChatColor.YELLOW + "Granted automatically!");
                } else if (isLogin) {
                    lore.add(ChatColor.GRAY + "Repeats every: " + ChatColor.WHITE + status.getEntry().getTimeString());
                    lore.add(ChatColor.YELLOW + "Granted automatically on login!");
                } else {
                    lore.add(ChatColor.YELLOW + "Click to claim!");
                }
                break;
            case LOCKED:
                material = Material.BARRIER;
                nameColor = "&7";
                lore.add(ChatColor.GRAY + "Required time: " + ChatColor.WHITE + status.getEntry().getTimeString());
                lore.add(ChatColor.GRAY + "Time remaining: " + ChatColor.WHITE + formatDuration(status.getMillisRemaining()));
                break;
            case COOLDOWN:
                material = Material.CLOCK;
                nameColor = "&e";
                lore.add(ChatColor.GRAY + "Already claimed.");
                if (isLogin) {
                    lore.add(ChatColor.GRAY + "Repeats every: " + ChatColor.WHITE + status.getEntry().getTimeString());
                }
                lore.add(ChatColor.GRAY + "Available again in: " + ChatColor.WHITE + formatDuration(status.getMillisRemaining()));
                break;
            case NO_PERMISSION:
            default:
                material = Material.BARRIER;
                nameColor = "&c";
                lore.add(ChatColor.RED + "You don't have access to this reward.");
                break;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    nameColor + status.getEntry().getId() + " &7(" + type + ")"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
