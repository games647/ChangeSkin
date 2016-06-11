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

        UserPreferences preferences = plugin.getStorage().getPreferences(playerUuid);
        plugin.getCore().startSession(playerUuid, preferences);
        if (preferences.getTargetSkin() == null && plugin.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(playerName, preferences);
        }
    }

    private void refetchSkin(String playerName, final UserPreferences preferences) {
        UUID ownerUUID = plugin.getCore().getUuidCache().get(playerName);
        if (ownerUUID == null) {
            ownerUUID = plugin.getCore().getUUID(playerName);
            if (ownerUUID != null) {
                plugin.getCore().getUuidCache().put(playerName, ownerUUID);
            }
        }

        if (ownerUUID != null) {
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID);
            if (cachedSkin == null) {
                cachedSkin = plugin.getCore().downloadSkin(ownerUUID);
                plugin.getCore().getUuidCache().put(cachedSkin.getName(), cachedSkin.getUuid());
            }

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
