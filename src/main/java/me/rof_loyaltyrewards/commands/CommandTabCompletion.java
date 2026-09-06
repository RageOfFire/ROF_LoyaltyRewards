package me.rof_loyaltyrewards.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandTabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> commandtab = new ArrayList<>();
            commandtab.add("reload");
            commandtab.add("gui");
            commandtab.add("rewards");
            return commandtab;
        }
        return null;
    }
}
