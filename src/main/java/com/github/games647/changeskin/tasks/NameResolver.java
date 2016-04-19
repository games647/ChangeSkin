package com.github.games647.changeskin.tasks;

import com.github.games647.changeskin.ChangeSkin;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameResolver implements Runnable {

    private final ChangeSkin plugin;
    private final CommandSender invoker;
    private final String targetName;
    private final Player player;

    public NameResolver(ChangeSkin plugin, CommandSender invoker, String targetName, Player targetPlayer) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.player = targetPlayer;
    }

    @Override
    public void run() {
        UUID cachedUUID = plugin.getUuidCache().get(targetName);
        if (cachedUUID == null) {
            cachedUUID = plugin.getUUID(targetName);
            if (cachedUUID == null) {
                if (invoker != null) {
                    invoker.sendMessage(ChatColor.DARK_RED + "UUID couldn't be resolved");
                }
            } else {
                plugin.getUuidCache().put(targetName, cachedUUID);
            }
        }

        if (cachedUUID != null) {
            if (invoker != null) {
                invoker.sendMessage(ChatColor.DARK_GREEN + "UUID was successfull resolved from the player name");
                if (plugin.getConfig().getBoolean("skinPermission")
                        && !invoker.hasPermission(plugin.getName().toLowerCase() + ".skin." + cachedUUID.toString())
                        && !invoker.hasPermission(plugin.getName().toLowerCase() + ".skin.*")) {
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
