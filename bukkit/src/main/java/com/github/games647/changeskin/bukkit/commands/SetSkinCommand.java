package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.NameResolver;
import com.github.games647.changeskin.bukkit.tasks.SkinDownloader;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSkinCommand extends AbstractForwardCommand {

    public SetSkinCommand(ChangeSkinBukkit plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.isBungeeCord()) {
            onBungeeCord(sender, command.getName(), args);
            return true;
        }

        if (isCooldown(sender)) {
            plugin.sendMessage(sender, "cooldown");
            return true;
        }

        if (args.length > 0 && "set".equalsIgnoreCase(args[0])) {
            args = Arrays.copyOfRange(args, 1, args.length);
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
                setSkin(sender, targetPlayer, toSkin, isKeepSkin(args));
            }
        } else if (sender instanceof Player) {
            if (args.length == 1) {
                if ("reset".equalsIgnoreCase(args[0])) {
                    setSkinUUID(sender, (Player) sender, ((Player) sender).getUniqueId().toString(), isKeepSkin(args));
                    return true;
                }

                setSkin(sender, (Player) sender, args[0], isKeepSkin(args));
            } else {
                plugin.sendMessage(sender, "no-skin");
            }
        } else {
            plugin.sendMessage(sender, "no-console");
        }

        return true;
    }

    private boolean isCooldown(CommandSender sender) {
        return sender instanceof Player && plugin.getCore().isCooldown(((Player) sender).getUniqueId());
    }

    private void setSkin(CommandSender sender, Player targetPlayer, String toSkin, boolean keepSkin) {
        //minecraft player names has the max length of 16 characters so it could be the uuid
        if (toSkin.length() > 16) {
            setSkinUUID(sender, targetPlayer, toSkin, keepSkin);
        } else {
            plugin.sendMessage(sender, "queue-name-resolve");
            Runnable nameResolver = new NameResolver(plugin, sender, toSkin, targetPlayer, keepSkin);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, nameResolver);
        }
    }

    private void setSkinUUID(CommandSender sender, Player receiverPayer, String targetUUID, boolean keepSkin) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(sender, uuid, true)) {
                return;
            }

            plugin.sendMessage(sender, "skin-change-queue");
            Runnable skinDownloader = new SkinDownloader(plugin, sender, receiverPayer, uuid, keepSkin);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, skinDownloader);
        } catch (IllegalArgumentException illegalArgumentException) {
            plugin.sendMessage(sender, "invalid-uuid");
        }
    }



    private boolean isKeepSkin(String... args) {
        if (args.length > 0) {
            String lastArg = args[args.length - 1];
            return "keep".equalsIgnoreCase(lastArg);
        }

        return false;
    }
}
