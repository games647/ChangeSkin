package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.task.SkinInvalidator;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class InvalidateCommand extends Command {

    private final ChangeSkinBungee plugin;

    public InvalidateCommand(ChangeSkinBungee plugin) {
        super("skinupdate",
                plugin.getName().toLowerCase() + ".command.skinupdate",
                "skin-update");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean isOp = sender.getGroups().contains(plugin.getName() + "-OP");
        if (sender instanceof ProxiedPlayer) {
            sender.removeGroups(plugin.getName() + "-OP");
        }
        
        if (args.length > 0) {
            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
                return;
            }

            String permPrefix = plugin.getName().toLowerCase() + ".command.skinupdate.other.";
            if (!sender.hasPermission(permPrefix + targetPlayer.getUniqueId())
                    && !sender.hasPermission(permPrefix + '*')) {
                plugin.sendMessage(sender, "no-permission-other");
                return;
            }

            Runnable skinInvalidate = new SkinInvalidator(plugin, sender, targetPlayer, isOp);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, skinInvalidate);
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            plugin.sendMessage(sender, "no-console");
            return;
        }

        ProxiedPlayer receiver = (ProxiedPlayer) sender;
        Runnable skinInvalidate = new SkinInvalidator(plugin, sender, receiver, isOp);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinInvalidate);
    }
}
