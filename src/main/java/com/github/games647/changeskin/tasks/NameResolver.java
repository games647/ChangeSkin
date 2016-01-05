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

    public NameResolver(ChangeSkin plugin, CommandSender invoker, String targetName, Player player) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.player = player;
    }

    @Override
    public void run() {
        UUID cachedUUID = plugin.getUuidCache().get(targetName);
        if (cachedUUID == null) {
            cachedUUID = plugin.getUUID(targetName);
            if (cachedUUID != null) {
                plugin.getUuidCache().put(targetName, cachedUUID);
            }
        }

        if (cachedUUID != null) {
            //Save the target uuid from the requesting player source
            plugin.getUserPreferences().put(player.getUniqueId(), cachedUUID);
            if (invoker != null) {
                invoker.sendMessage(ChatColor.DARK_GREEN + "UUID was successfull resolved from the player name");
                invoker.sendMessage(ChatColor.DARK_GREEN + "The skin is now downloading");
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, cachedUUID).run();
        }
    }
}
