package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.bukkit.tasks.NameResolver;
import com.github.games647.changeskin.bukkit.tasks.SkinDownloader;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSkinCommand implements CommandExecutor {

    protected final ChangeSkinBukkit plugin;

    public SetSkinCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (isCooldown(sender)) {
            plugin.sendMessage(sender, "cooldown");
            return true;
        }

        if (args.length > 1) {
            if (!sender.hasPermission(command.getPermission() + ".other")) {
                plugin.sendMessage(sender, "no-permission-other");
                return true;
            }

            String targetPlayerName = args[0];
            String toSkin = args[1];

            Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
            } else {
                setSkin(sender, targetPlayer, toSkin);
            }
        } else if (sender instanceof Player) {
            if (args.length == 1) {
                if ("reset".equalsIgnoreCase(args[0])) {
                    setSkinUUID(sender, (Player) sender, ((Player) sender).getUniqueId().toString());
                    return true;
                }

                setSkin(sender, (Player) sender, args[0]);
            } else {
                plugin.sendMessage(sender, "no-skin");
            }
        } else {
            plugin.sendMessage(sender, "no-console");
        }

        return true;
    }

    private boolean isCooldown(CommandSender sender) {
        return sender instanceof Player && plugin.isCooldown(((Player) sender).getUniqueId());
    }

    private void setSkin(CommandSender sender, Player targetPlayer, String toSkin) {
        //minecraft player names has the max length of 16 characters so it could be the uuid
        if (toSkin.length() > 16) {
            setSkinUUID(sender, targetPlayer, toSkin);
        } else {
            plugin.sendMessage(sender, "queue-name-resolve");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new NameResolver(plugin, sender, toSkin, targetPlayer));
        }
    }

    private void setSkinUUID(CommandSender sender, Player receiverPayer, String targetUUID) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(sender, uuid)) {
                plugin.sendMessage(sender, "no-permission");
                return;
            }

            plugin.sendMessage(sender, "skin-change-queue");

            SkinDownloader skinDownloader = new SkinDownloader(plugin, sender, receiverPayer, uuid);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, skinDownloader);
        } catch (IllegalArgumentException illegalArgumentException) {
            plugin.sendMessage(sender, "invalid-uuid");
        }
    }
}
