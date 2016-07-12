package com.github.games647.changeskin.bungee.commands;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.tasks.NameResolver;
import com.github.games647.changeskin.bungee.tasks.SkinDownloader;

import java.util.Arrays;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SetSkinCommand extends Command {

    protected final ChangeSkinBungee plugin;

    public SetSkinCommand(ChangeSkinBungee plugin) {
        super("setskin", plugin.getDescription().getName().toLowerCase() + ".command.setskin"
                , "skin", plugin.getDescription().getName());

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean isOp = sender.getGroups().contains(plugin.getName() + "-OP");
        sender.removeGroups(plugin.getName() + "-OP");

        if (sender instanceof ProxiedPlayer && plugin.isCooldown(((ProxiedPlayer) sender).getUniqueId())) {
            plugin.sendMessage(sender, "cooldown");
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length > 1) {
            if (!sender.hasPermission(plugin.getDescription().getName().toLowerCase() + ".command.setskin.other")
                    && !sender.hasPermission(plugin.getDescription().getName().toLowerCase() + ".command.setskin.*")) {
                plugin.sendMessage(sender, "no-permission-other");
                return;
            }

            String targetPlayerName = args[0];
            String toSkin = args[1];

            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetPlayerName);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
            } else {
                setSkin(sender, targetPlayer, toSkin, isOp);
            }
        } else if (sender instanceof ProxiedPlayer) {
            if (args.length == 1) {
                if ("reset".equalsIgnoreCase(args[0])) {
                    onReset(sender, isOp);
                    return;
                }

                setSkin(sender, (ProxiedPlayer) sender, args[0], isOp);
            } else {
                plugin.sendMessage(sender, "no-skin");
            }
        } else {
            plugin.sendMessage(sender, "no-console");
        }
    }

    private void onReset(CommandSender sender, boolean bukkitOp) {
        setSkinUUID(sender, (ProxiedPlayer) sender, ((ProxiedPlayer) sender).getUniqueId().toString(), bukkitOp);
    }

    private void setSkin(CommandSender sender, ProxiedPlayer targetPlayer, String toSkin, boolean bukkitOp) {
        //minecraft player names has the max length of 16 characters so it could be the uuid
        if (toSkin.length() > 16) {
            setSkinUUID(sender, targetPlayer, toSkin, bukkitOp);
        } else {
            plugin.sendMessage(sender, "queue-name-resolve");
            NameResolver nameResolver = new NameResolver(plugin, sender, toSkin, targetPlayer, bukkitOp);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, nameResolver);
        }
    }

    private void setSkinUUID(CommandSender sender, ProxiedPlayer receiverPayer, String targetUUID, boolean bukkitOp) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(sender, uuid)) {
                return;
            }

            plugin.sendMessage(sender, "skin-change-queue");

            SkinDownloader skinDownloader = new SkinDownloader(plugin, sender, receiverPayer, uuid, bukkitOp);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, skinDownloader);
        } catch (IllegalArgumentException illegalArgumentException) {
            plugin.sendMessage(sender, "invalid-uuid");
        }
    }
}
