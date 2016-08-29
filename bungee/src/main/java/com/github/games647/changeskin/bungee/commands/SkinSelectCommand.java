package com.github.games647.changeskin.bungee.commands;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.tasks.SkinSelector;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SkinSelectCommand extends Command {

    private final ChangeSkinBungee plugin;

    public SkinSelectCommand(ChangeSkinBungee plugin) {
        super("skin-select", plugin.getDescription().getName().toLowerCase() + ".command.skinselect", "skinselect");

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
                SkinSelector skinSelector = new SkinSelector(plugin, (ProxiedPlayer) sender, targetId);
                BungeeCord.getInstance().getScheduler().runAsync(plugin, skinSelector);
            } catch (NumberFormatException numberFormatException) {
                plugin.sendMessage(sender, "invalid-skin-name");
            }
        }
    }
}
