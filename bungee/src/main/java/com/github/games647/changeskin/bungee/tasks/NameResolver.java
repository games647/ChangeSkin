package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;

import java.util.UUID;

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
        UUID uuid = plugin.getCore().getUuidCache().get(targetName);
        if (uuid == null) {
            uuid = plugin.getCore().getUUID(targetName);
            if (uuid == null) {
                if (invoker != null) {
                    plugin.sendMessage(invoker, "no-resolve");
                }
            } else {
                plugin.getCore().getUuidCache().put(targetName, uuid);
            }
        }

        if (uuid != null) {
            if (invoker != null) {
                plugin.sendMessage(invoker, "uuid-resolved");
                if (plugin.getConfiguration().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid)) {
                    return;
                }

                plugin.sendMessage(invoker, "skin-downloading");
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, player, uuid).run();
        }
    }
}
