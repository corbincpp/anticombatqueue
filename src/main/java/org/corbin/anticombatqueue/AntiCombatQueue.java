package org.corbin.anticombatqueue;

import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AntiCombatQueue extends JavaPlugin implements Listener, CommandExecutor {

    private boolean isEnabled;
    private List<String> blacklistedCommands;
    private String combatMessage;
    private DeluxeCombatAPI deluxeCombatAPI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("anticombatqueue").setExecutor(this);

        // Check if DeluxeCombat is enabled
        if (Bukkit.getPluginManager().getPlugin("DeluxeCombat") != null) {
            deluxeCombatAPI = new DeluxeCombatAPI();
            getLogger().info("DeluxeCombatAPI hooked successfully.");
        } else {
            getLogger().severe("DeluxeCombat plugin not found. Disabling AntiCombatQueue.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        isEnabled = config.getBoolean("Enabled", true);
        blacklistedCommands = config.getStringList("BlacklistedCommands");
        combatMessage = ChatColor.translateAlternateColorCodes('&', config.getString("CombatMessage", "&cYou cannot do this, you are still in combat!"));
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!isEnabled || deluxeCombatAPI == null) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        for (String command : blacklistedCommands) {
            if (message.startsWith(command.toLowerCase())) {
                if (deluxeCombatAPI.isInCombat(player)) {
                    player.sendMessage(combatMessage);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("anticombatqueue")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                loadConfig();
                sender.sendMessage(ChatColor.GREEN + "AntiCombatQueue configuration reloaded.");
                return true;
            }
        }
        return false;
    }
}