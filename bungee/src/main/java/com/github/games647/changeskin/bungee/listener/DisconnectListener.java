package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DisconnectListener implements Listener {

    private final ChangeSkinBungee plugin;

    public DisconnectListener(ChangeSkinBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        plugin.endSession(disconnectEvent.getPlayer().getPendingConnection());
    }
}
