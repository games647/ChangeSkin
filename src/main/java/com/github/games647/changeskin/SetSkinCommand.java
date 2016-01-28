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
        if (args.length > 1) {
            String targetPlayerName = args[0];
            String toSkin = args[1];

            Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.DARK_RED + "This player isn't online");
            } else {
                setSkinOther(sender, targetPlayer, toSkin);
            }
        } else if (sender instanceof Player) {
            if (args.length == 1) {
                setSkinOther(sender, (Player) sender, args[0]);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You have to provide the skin you want to change to");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You have to be a player to set your own skin");
        }

        return true;
    }

    private void setSkinOther(CommandSender sender, Player targetPlayer, String toSkin) {
        //minecraft player names has the max length of 16 characters so it could be the uuid
        if (toSkin.length() > 16) {
            setSkinUUID(sender, targetPlayer, toSkin);
        } else {
            sender.sendMessage(ChatColor.GOLD + "Queued name to uuid resolve");
            Bukkit.getScheduler()
                    .runTaskAsynchronously(plugin, new NameResolver(plugin, sender, toSkin, targetPlayer));
        }
    }

    private void setSkinUUID(CommandSender sender, Player targetPlayer, String targetUUID) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (targetPlayer.getUniqueId().equals(uuid)) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Reseting preferences to the default value");
                plugin.getUserPreferences().remove(targetPlayer.getUniqueId());
            } else {
                sender.sendMessage(ChatColor.GOLD + "Queued Skin change");
                plugin.getUserPreferences().put(targetPlayer.getUniqueId(), uuid);
                
                SkinDownloader skinDownloader = new SkinDownloader(plugin, sender, targetPlayer, uuid);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, skinDownloader);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            sender.sendMessage(ChatColor.DARK_RED + "Invalid uuid");
        }
    }
}
