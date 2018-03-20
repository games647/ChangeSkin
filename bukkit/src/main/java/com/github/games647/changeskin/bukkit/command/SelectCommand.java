package com.github.games647.changeskin.bukkit.command;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.task.SkinSelector;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectCommand implements CommandExecutor {

    private final ChangeSkinBukkit plugin;

    public SelectCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "no-console");
            return true;
        }

        if (args.length == 0) {
            plugin.sendMessage(sender, "select-noargs");
        } else {
            String targetName = args[0].toLowerCase().replace("skin-", "");
            try {
                int targetId = Integer.parseInt(targetName);
                Runnable task = new SkinSelector(plugin, (Player) sender, targetId);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            } catch (NumberFormatException numberFormatException) {
                plugin.sendMessage(sender, "invalid-skin-name");
            }
        }

        return true;
    }
}
