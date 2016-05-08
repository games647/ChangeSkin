package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameResolver implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final String targetName;
    private final Player player;

    public NameResolver(ChangeSkinBukkit plugin, CommandSender invoker, String targetName, Player targetPlayer) {
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
                    invoker.sendMessage(ChatColor.DARK_RED + "UUID couldn't be resolved");
                }
            } else {
                plugin.getCore().getUuidCache().put(targetName, uuid);
            }
        }

        if (uuid != null) {
            if (invoker != null) {
                invoker.sendMessage(ChatColor.DARK_GREEN + "UUID was successfull resolved from the player name");
                if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid)) {
                    return;
                }

                invoker.sendMessage(ChatColor.DARK_GREEN + "The skin is now downloading");
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, player, uuid).run();
        }
    }
}
