package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinInvalidator;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvalidateCommand extends AbstractForwardCommand {

    public InvalidateCommand(ChangeSkinBukkit plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.isBungeeCord()) {
            onBungeeCord(sender, command.getName(), args);
            return true;
        }

        if (args.length > 0) {
            Player targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
                return true;
            }

            String permPrefix = plugin.getName().toLowerCase() + ".command.skinupdate.other.";
            if (!sender.hasPermission(permPrefix + targetPlayer.getUniqueId())
                    && !sender.hasPermission(permPrefix + '*')) {
                plugin.sendMessage(sender, "no-permission-other");
                return true;
            }

            Runnable skinInvalidate = new SkinInvalidator(plugin, sender, targetPlayer);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, skinInvalidate);
            return true;
        }

        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "no-console");
            return true;
        }

        Player receiver = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new SkinInvalidator(plugin, sender, receiver));
        return true;
    }
}
