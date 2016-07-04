package com.github.games647.changeskin.bungee.commands;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.tasks.SkinInvalidater;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SkinInvalidateCommand extends Command {

    private final ChangeSkinBungee plugin;

    public SkinInvalidateCommand(ChangeSkinBungee plugin) {
        super("skinupdate", plugin.getDescription().getName().toLowerCase() + ".command.skinupdate");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
            if (targetPlayer == null) {
                plugin.sendMessage(sender, "not-online");
                return;
            }

            SkinInvalidater skinInvalidater = new SkinInvalidater(plugin, sender, targetPlayer);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, skinInvalidater);
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            plugin.sendMessage(sender, "no-console");
            return;
        }

        ProxiedPlayer receiver = (ProxiedPlayer) sender;
        SkinInvalidater skinInvalidater = new SkinInvalidater(plugin, sender, receiver);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinInvalidater);
    }
}
