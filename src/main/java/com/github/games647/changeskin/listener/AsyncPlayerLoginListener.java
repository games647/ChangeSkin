package com.github.games647.changeskin.listener;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerLoginListener implements Listener {

    private final ChangeSkin plugin;

    public AsyncPlayerLoginListener(ChangeSkin plugin) {
        this.plugin = plugin;
    }

    //we are making an blocking request it might be better to ignore it if normal priority events cancelled it
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent preLoginEvent) {
        if (preLoginEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        UUID playerUuid = preLoginEvent.getUniqueId();
        String playerName = preLoginEvent.getName();

        UUID targetUuid = plugin.getUserPreferences().get(playerUuid);
        if (targetUuid == null) {
            if (plugin.getConfig().getBoolean("restoreSkins") && !plugin.getSkinCache().containsKey(playerUuid)) {
                refetchSkin(playerName, playerUuid);
            }
        } else if (!plugin.getSkinCache().containsKey(targetUuid)) {
            //player selected a custom skin which isn't in the cache. Try to download it
            WrappedSignedProperty downloadedSkin = plugin.downloadSkin(targetUuid);
            if (downloadedSkin != null) {
                //run it blocking because we don't know how it takes, so it won't end into a race condition
                plugin.getSkinCache().put(targetUuid, downloadedSkin);
            }
        }
    }

    private void refetchSkin(String playerName, UUID playerUUID) {
        UUID ownerUUID = plugin.getUuidCache().get(playerName);
        if (ownerUUID == null) {
            ownerUUID = plugin.getUUID(playerName);
            if (ownerUUID != null) {
                plugin.getUuidCache().put(playerName, ownerUUID);
            }
        }

        if (ownerUUID != null) {
            WrappedSignedProperty cachedSkin = plugin.getSkinCache().get(ownerUUID);
            if (cachedSkin == null) {
                cachedSkin = plugin.downloadSkin(ownerUUID);
                if (cachedSkin != null) {
                    plugin.getSkinCache().put(ownerUUID, cachedSkin);
                    plugin.getUserPreferences().put(playerUUID, ownerUUID);
                }
            }
        }
    }
}
