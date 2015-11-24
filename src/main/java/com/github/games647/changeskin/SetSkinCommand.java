package com.github.games647.changeskin;

import java.util.UUID;

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
                setSkinUUID((Player) sender, args[0]);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You have to provide the skin you want to change to");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You have to be a player to set your own skin");
        }

        return true;
    }

    private void setSkinUUID(Player player, String targetSource) {
        try {
            UUID uuid = UUID.fromString(targetSource);
            player.sendMessage("Queued Skin change");
            plugin.getUserPreferences().put(player.getUniqueId(), uuid);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new SkinDownloader(plugin, player, uuid));
        } catch (IllegalArgumentException illegalArgumentException) {
            player.sendMessage(ChatColor.DARK_RED + "Invalid uuid");
        }
    }
}
