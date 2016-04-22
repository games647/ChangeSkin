package com.github.games647.changeskin.listener;

import com.github.games647.changeskin.ChangeSkin;
import com.github.games647.changeskin.SkinData;
import com.github.games647.changeskin.UserPreferences;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerLoginListener implements Listener {

    protected final ChangeSkin plugin;

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

        UserPreferences preferences = plugin.getStorage().getPreferences(playerUuid, true);
        if (preferences.getTargetSkin() == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                refetchSkin(playerName, playerUuid);
            }
        }
    }

    private void refetchSkin(String playerName, UUID receiverUUID) {
        UUID ownerUUID = plugin.getUuidCache().get(playerName);
        if (ownerUUID == null) {
            ownerUUID = plugin.getUUID(playerName);
            if (ownerUUID != null) {
                plugin.getUuidCache().put(playerName, ownerUUID);
            }
        }

        if (ownerUUID != null) {
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID, true);
            if (cachedSkin == null) {
                cachedSkin = plugin.downloadSkin(ownerUUID);
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
                    plugin.getStorage().save(skin);
                    plugin.getStorage().save(preferences);
                }
            });
        }
    }
}
