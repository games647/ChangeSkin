package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinApplier;
import com.github.games647.changeskin.core.shared.SharedBungeeListener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeListener extends SharedBungeeListener<Player> implements PluginMessageListener {

    private final ChangeSkinBukkit plugin;

    public BungeeListener(ChangeSkinBukkit plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(channelName)) {
            return;
        }

        handlePayload(player, message);
    }

    @Override
    protected void sendMessage(Player player, String channel, byte[] data) {
        player.sendPluginMessage(plugin, channel, data);
    }

    @Override
    protected void runUpdater(Player receiver) {
        Bukkit.getScheduler().runTask(plugin, new SkinApplier(plugin, null, receiver, null, false));
    }

    @Override
    protected Player getPlayerExact(String name) {
        return Bukkit.getPlayerExact(name);
    }

    @Override
    protected UUID getUUID(Player player) {
        return player.getUniqueId();
    }

    @Override
    protected boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    protected boolean checkWhitelistPermission(Player player, UUID targetUUID) {
        return plugin.hasSkinPermission(player, targetUUID, false);
    }
}
