package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

import java.util.UUID;
import java.util.logging.Level;

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

        UserPreference preferences = plugin.getStorage().getPreferences(playerUuid);
        plugin.startSession(playerUuid, preferences);
        if (preferences.getTargetSkin() == null && plugin.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(playerName, preferences);
        }
    }

    private void refetchSkin(String playerName, UserPreference preferences) {
        UUID ownerUUID = plugin.getCore().getUuidCache().get(playerName);
        if (ownerUUID == null && !plugin.getCore().getCrackedNames().containsKey(playerName)) {
            SkinData skin = plugin.getStorage().getSkin(playerName);
            if (skin != null) {
                preferences.setTargetSkin(skin);
                save(skin, preferences);
                return;
            }

            try {
                ownerUUID = plugin.getCore().getMojangSkinApi().getUUID(playerName);
            } catch (NotPremiumException ex) {
                plugin.getLogger().log(Level.FINE, "Username is not premium on refetch", ex);
                plugin.getCore().getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                plugin.getLogger().log(Level.SEVERE, "Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            plugin.getCore().getUuidCache().put(playerName, ownerUUID);
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID);
            if (cachedSkin == null) {
                cachedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(ownerUUID);
            }

            preferences.setTargetSkin(cachedSkin);
            save(cachedSkin, preferences);
        }
    }

    private void save(final SkinData skin, final UserPreference preferences) {
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
