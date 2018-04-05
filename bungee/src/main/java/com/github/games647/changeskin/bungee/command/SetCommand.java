package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.task.NameResolver;
import com.github.games647.changeskin.bungee.task.SkinDownloader;

import java.util.Arrays;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SetCommand extends Command {

    protected final ChangeSkinBungee plugin;

    public SetCommand(ChangeSkinBungee plugin) {
        super("setskin", plugin.getName().toLowerCase() + ".command.setskin", "skin"
                , plugin.getName(), "skin", "set-skin");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean isOp = sender.getGroups().contains(plugin.getName() + "-OP");
        if (sender instanceof ProxiedPlayer) {
            sender.removeGroups(plugin.getName() + "-OP");

            if (plugin.getCore().getCooldownService().isTracked(((ProxiedPlayer) sender).getUniqueId())) {
                plugin.sendMessage(sender, "cooldown");
                return;
            }
        }

        if (args.length > 0 && "set".equalsIgnoreCase(args[0])) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length > 1) {
            if (!sender.hasPermission(plugin.getName().toLowerCase() + ".command.setskin.other")
                    && !sender.hasPermission(plugin.getName().toLowerCase() + ".command.setskin.*")) {
                plugin.sendMessage(sender, "no-permission-other");
                return;
            }

            String targetPlayerName = args[0];
            String toSkin = args[1];

            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetPlayerName);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
            } else {
                setSkin(sender, targetPlayer, toSkin, isOp, isKeepSkin(args));
            }
        } else if (sender instanceof ProxiedPlayer) {
            if (args.length == 1) {
                if ("reset".equalsIgnoreCase(args[0])) {
                    onReset(sender, isOp);
                    return;
                }

                setSkin(sender, (ProxiedPlayer) sender, args[0], isOp, isKeepSkin(args));
            } else {
                plugin.sendMessage(sender, "no-skin");
            }
        } else {
            plugin.sendMessage(sender, "no-console");
        }
    }

    private void onReset(CommandSender sender, boolean bukkitOp) {
        setSkinUUID(sender, (ProxiedPlayer) sender, ((ProxiedPlayer) sender).getUniqueId().toString(), bukkitOp, false);
    }

    private void setSkin(CommandSender sender, ProxiedPlayer targetPlayer, String toSkin,
                         boolean bukkitOp, boolean keepSkin) {
        //Minecraft player names has the max length of 16 characters so it could be the uuid
        if (toSkin.length() > 16) {
            setSkinUUID(sender, targetPlayer, toSkin, bukkitOp, keepSkin);
        } else {
            Runnable nameResolver = new NameResolver(plugin, sender, targetPlayer, toSkin, bukkitOp, keepSkin);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, nameResolver);
        }
    }

    private void setSkinUUID(CommandSender sender, ProxiedPlayer receiverPayer, String targetUUID,
                             boolean bukkitOp, boolean keepSkin) {
        try {
            UUID uuid = UUID.fromString(targetUUID);
            if (plugin.getCore().getConfig().getBoolean("skinPermission")
                    && !plugin.hasSkinPermission(sender, uuid, true)) {
                return;
            }

            plugin.sendMessage(sender, "skin-change-queue");

            Runnable skinDownloader = new SkinDownloader(plugin, sender, receiverPayer, uuid, bukkitOp, keepSkin);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, skinDownloader);
        } catch (IllegalArgumentException illegalArgumentException) {
            plugin.sendMessage(sender, "invalid-uuid");
        }
    }

    private boolean isKeepSkin(String[] args) {
        if (args.length > 0) {
            String lastArg = args[args.length - 1];
            return "keep".equalsIgnoreCase(lastArg);
        }

        return false;
    }
}
