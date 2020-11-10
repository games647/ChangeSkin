package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.task.SkinSelector;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SelectCommand extends ChangeSkinCommand {

    private final ChangeSkinBungee plugin;

    public SelectCommand(ChangeSkinBungee plugin) {
        super("skin-select", plugin.getName().toLowerCase() + ".command.skinselect", "skinselect");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.sendMessage(sender, "no-console");
            return;
        }

        if (args.length == 0) {
            plugin.sendMessage(sender, "select-noargs");
        } else {
            String targetName = args[0].toLowerCase().replace("skin-", "");
            try {
                int targetId = Integer.parseInt(targetName);
                Runnable skinSelector = new SkinSelector(plugin, (ProxiedPlayer) sender, targetId);
                ProxyServer.getInstance().getScheduler().runAsync(plugin, skinSelector);
            } catch (NumberFormatException numberFormatException) {
                plugin.sendMessage(sender, "invalid-skin-name");
            }
        }
    }
}
