package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NameResolver implements Runnable {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final String targetName;
    private final ProxiedPlayer player;

    public NameResolver(ChangeSkinBungee plugin, CommandSender invoker, String targetName, ProxiedPlayer targetPlayer) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.player = targetPlayer;
    }

    @Override
    public void run() {
        UUID uuid = plugin.getCore().getUuidCache().get(targetName);
        if (uuid == null) {
            uuid = plugin.getCore().getUUID(targetName);
            if (uuid == null) {
                if (invoker != null) {
                    invoker.sendMessage(new ComponentBuilder("UUID couldn't be resolved")
                        .color(ChatColor.DARK_RED).create());
                }
            } else {
                plugin.getCore().getUuidCache().put(targetName, uuid);
            }
        }

        if (uuid != null) {
            if (invoker != null) {
                invoker.sendMessage(new ComponentBuilder("UUID was successfull resolved from the player name")
                        .color(ChatColor.DARK_GREEN).create());

                if (plugin.getConfiguration().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid)) {
                    return;
                }

                invoker.sendMessage(new ComponentBuilder("The skin is now downloading")
                        .color(ChatColor.DARK_GREEN).create());
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, player, uuid).run();
        }
    }
}
