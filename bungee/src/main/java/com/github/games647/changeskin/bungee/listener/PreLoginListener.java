package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PreLoginListener implements Listener {

    protected final ChangeSkinBungee plugin;

    public PreLoginListener(ChangeSkinBungee changeSkinBungee) {
        this.plugin = changeSkinBungee;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(LoginEvent loginEvent) {
        if (loginEvent.isCancelled()) {
            return;
        }

        PendingConnection connection = loginEvent.getConnection();
        UUID playerUuid = connection.getUniqueId();
        String playerName = connection.getName();

        UserPreferences preferences = plugin.getStorage().getPreferences(playerUuid, true);
        if (preferences.getTargetSkin() == null) {
            if (plugin.getConfiguration().getBoolean("restoreSkins")) {
                refetchSkin(playerName, playerUuid, loginEvent);
            }
        }
    }

     private void refetchSkin(final String playerName, final UUID receiverUUID, final AsyncEvent<?> preLoginEvent) {
        preLoginEvent.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                refetch(playerName, receiverUUID);
                preLoginEvent.completeIntent(plugin);
            }
        });
    }

    private void refetch(String playerName, UUID receiverUUID) {
        UUID ownerUUID = plugin.getCore().getUuidCache().get(playerName);
        if (ownerUUID == null) {
            ownerUUID = plugin.getCore().getUUID(playerName);
            if (ownerUUID != null) {
                plugin.getCore().getUuidCache().put(playerName, ownerUUID);
            }
        }

        if (ownerUUID != null) {
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID, true);
            if (cachedSkin == null) {
                cachedSkin = plugin.getCore().downloadSkin(ownerUUID);
                if (cachedSkin != null) {
                    plugin.getStorage().getSkinUUIDCache().put(ownerUUID, cachedSkin);
                }
            }

            final UserPreferences preferences = plugin.getStorage().getPreferences(receiverUUID, true);
            preferences.setTargetSkin(cachedSkin);

            final SkinData skin = cachedSkin;

            //this can run in the background
            BungeeCord.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                @Override
                public void run() {
                    if (plugin.getStorage().save(skin)) {
                        plugin.getStorage().save(preferences);
                    }
                }
            });
        }
    }
}
