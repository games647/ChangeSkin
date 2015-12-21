package com.github.games647.changeskin;

import com.github.games647.changeskin.tasks.NameResolver;
import com.github.games647.changeskin.tasks.SkinDownloader;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSkinCommand implements CommandExecutor {

    private final ChangeSkin plugin;

    public SetSkinCommand(ChangeSkin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length > 0) {
                String targetSource = args[0];
                //minecraft player names has the max length of 16 characters
                if (targetSource.length() > 16) {
                    setSkinUUID((Player) sender, targetSource);
                } else {
                    setSkinName((Player) sender, targetSource);
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You have to provide the skin you want to change to");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You have to be a player to set your own skin");
        }

        return true;
    }

    private void setSkinUUID(Player player, String targetUUID) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (player.getUniqueId().equals(uuid)) {
                player.sendMessage(ChatColor.DARK_GREEN + "Reseting your preferences");
                plugin.getUserPreferences().remove(player.getUniqueId());
            } else {
                player.sendMessage(ChatColor.GOLD + "Queued Skin change");
                plugin.getUserPreferences().put(player.getUniqueId(), uuid);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new SkinDownloader(plugin, player, uuid));
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            player.sendMessage(ChatColor.DARK_RED + "Invalid uuid");
        }
    }

    private void setSkinName(Player player, String targetName) {
        player.sendMessage(ChatColor.GOLD + "Queued name to uuid resolve");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new NameResolver(plugin, targetName, player));
    }
}
