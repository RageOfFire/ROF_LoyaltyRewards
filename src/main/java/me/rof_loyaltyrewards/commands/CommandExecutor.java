package me.rof_loyaltyrewards.commands;

import me.rof_loyaltyrewards.ROF_LoyaltyRewards;
import me.rof_loyaltyrewards.gui.RewardGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    private final ROF_LoyaltyRewards plugin;
    private final RewardGUI rewardGUI;

    public CommandExecutor(ROF_LoyaltyRewards plugin, RewardGUI rewardGUI) {
        this.plugin = plugin;
        this.rewardGUI = rewardGUI;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String reloadmess = plugin.getConfig().getString("messages.reload", "&aReloaded ROF_LoyaltyRewards config.");
        String usagemess = plugin.getConfig().getString("messages.usage", "&cUsage: /rofloyaltyrewards <reload|gui>");

        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', usagemess));
            return true;
        }

        if (strings[0].equalsIgnoreCase("reload")) {
            if (commandSender.hasPermission("rofow.admin")) {
                plugin.reloadConfig();
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadmess));
                return true;
            }
            return false;
        }

        if (strings[0].equalsIgnoreCase("gui") || strings[0].equalsIgnoreCase("rewards")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("This command can only be used in-game.");
                return true;
            }
            if (!commandSender.hasPermission("rofow.gui")) {
                commandSender.sendMessage(ChatColor.RED + "You don't have permission to view rewards.");
                return true;
            }
            rewardGUI.open((Player) commandSender);
            return true;
        }

        return false;
    }
}
