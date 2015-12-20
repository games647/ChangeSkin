package com.github.games647.changeskin;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NameResolver implements Runnable {

    private final ChangeSkin plugin;
    private final String targetName;
    private final Player player;

    public NameResolver(ChangeSkin plugin, String targetName, Player player) {
        this.plugin = plugin;
        this.targetName = targetName;
        this.player = player;
    }

    @Override
    public void run() {
        UUID uuid = plugin.getUUID(targetName);
        if (uuid != null) {
            //Save the target uuid from the requesting player source
            plugin.getUserPreferences().put(player.getUniqueId(), uuid);

            player.sendMessage(ChatColor.DARK_GREEN + "UUID was successfull resolved from the player name");
            player.sendMessage(ChatColor.DARK_GREEN + "The skin is now downloading");
            //run this is the same thread
            new SkinDownloader(plugin, player, uuid).run();
        }
    }
}
