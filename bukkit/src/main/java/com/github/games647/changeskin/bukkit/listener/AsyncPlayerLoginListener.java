package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerLoginListener implements Listener {

    protected final ChangeSkinBukkit plugin;

    public AsyncPlayerLoginListener(ChangeSkinBukkit plugin) {
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

        UserPreferences preferences = plugin.getStorage().getPreferences(playerUuid, true);
        if (preferences.getTargetSkin() == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                refetchSkin(playerName, playerUuid);
            }
        }
    }

    private void refetchSkin(String playerName, UUID receiverUUID) {
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
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
