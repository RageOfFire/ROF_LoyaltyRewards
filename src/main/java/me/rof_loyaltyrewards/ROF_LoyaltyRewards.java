package me.rof_loyaltyrewards;

import me.rof_loyaltyrewards.commands.CommandTabCompletion;
import me.rof_loyaltyrewards.commands.CommandExecutor;
import me.rof_loyaltyrewards.events.PlayerJoinEventListener;
import me.rof_loyaltyrewards.events.PlayerQuitEventListener;
import me.rof_loyaltyrewards.gui.GUIListener;
import me.rof_loyaltyrewards.gui.RewardGUI;
import me.rof_loyaltyrewards.handle.rewardHandler;
import me.rof_loyaltyrewards.placeholder.RewardPlaceholderExpansion;
import me.rof_loyaltyrewards.tasks.OnlineRewardScheduler;
import me.rof_loyaltyrewards.utils.FileLog;
import me.rof_loyaltyrewards.utils.OnlineSessionManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ROF_LoyaltyRewards extends JavaPlugin {
    private rewardHandler rewardHandler;
    private FileLog fileLog;
    private OnlineSessionManager sessionManager;
    private OnlineRewardScheduler onlineRewardScheduler;

    @Override
    public void onEnable() {
        //Setup Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        sessionManager = new OnlineSessionManager();
        rewardHandler = new rewardHandler(this, sessionManager);
        fileLog = new FileLog(this);
        onlineRewardScheduler = new OnlineRewardScheduler(this, rewardHandler);
        RewardGUI rewardGUI = new RewardGUI(this, rewardHandler);

        // Bstats config
        int pluginId = 19429; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Listeners
        getServer().getPluginManager().registerEvents(
                new PlayerJoinEventListener(this, rewardHandler, rewardGUI, sessionManager, onlineRewardScheduler), this);
        getServer().getPluginManager().registerEvents(
                new PlayerQuitEventListener(sessionManager, onlineRewardScheduler), this);
        getServer().getPluginManager().registerEvents(new GUIListener(rewardHandler, rewardGUI), this);

        // Commands
        getCommand("rofloyaltyrewards").setExecutor(new CommandExecutor(this, rewardGUI));
        getCommand("rofloyaltyrewards").setTabCompleter(new CommandTabCompletion());

        // If the plugin is reloaded/enabled while players are already online
        // (e.g. /reload), start tracking their sessions and arm their next
        // online reward too - not just players who join afterward.
        for (Player player : Bukkit.getOnlinePlayers()) {
            sessionManager.startSession(player.getUniqueId());
            onlineRewardScheduler.scheduleNext(player);
        }

        // PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RewardPlaceholderExpansion(this, rewardHandler).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }
    }

    @Override
    public void onDisable() {
        if (onlineRewardScheduler != null) {
            onlineRewardScheduler.cancelAll();
        }
    }
}
