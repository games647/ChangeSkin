package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
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
        UUID cachedUUID = plugin.getCore().getUuidCache().get(targetName);
        if (cachedUUID == null) {
            cachedUUID = plugin.getCore().getUUID(targetName);
            if (cachedUUID == null) {
                if (invoker != null) {
                    invoker.sendMessage(ChatColor.DARK_RED + "UUID couldn't be resolved");
                }
            } else {
                plugin.getCore().getUuidCache().put(targetName, cachedUUID);
            }
        }

        if (cachedUUID != null) {
            if (invoker != null) {
                invoker.sendMessage(ChatColor.DARK_GREEN + "UUID was successfull resolved from the player name");
                if (plugin.getConfiguration().getBoolean("skinPermission")
                        && !invoker.hasPermission(plugin.getDescription().getName().toLowerCase()
                                + ".skin." + cachedUUID.toString())
                        && !invoker.hasPermission(plugin.getDescription().getName().toLowerCase() + ".skin.*")) {
                    invoker.sendMessage(ChatColor.DARK_RED + "You don't have the permission to set this skin");
                    return;
                }

                invoker.sendMessage(ChatColor.DARK_GREEN + "The skin is now downloading");
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, player, cachedUUID).run();
        }
    }
}
