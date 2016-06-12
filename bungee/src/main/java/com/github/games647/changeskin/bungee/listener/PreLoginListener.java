package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;
import java.util.logging.Level;

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

        UserPreferences preferences = plugin.getStorage().getPreferences(playerUuid);
        plugin.getCore().startSession(playerUuid, preferences);
        if (preferences.getTargetSkin() == null && plugin.getConfiguration().getBoolean("restoreSkins")) {
            refetchSkin(preferences, playerName, loginEvent);
        }
    }

     private void refetchSkin(final UserPreferences prefereces, final String playerName
             , final AsyncEvent<?> preLoginEvent) {
        preLoginEvent.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    refetch(prefereces, playerName);
                } finally {
                    preLoginEvent.completeIntent(plugin);
                }
            }
        });
    }

    private void refetch(final UserPreferences preferences, String playerName) {
        UUID ownerUUID = plugin.getCore().getUuidCache().get(playerName);
        if (ownerUUID == null && !plugin.getCore().getCrackedNames().containsKey(playerName)) {
            SkinData skin = plugin.getStorage().getSkin(playerName);
            if (skin != null) {
                plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
                preferences.setTargetSkin(skin);
                save(skin, preferences);
                return;
            }

            try {
                ownerUUID = plugin.getCore().getUUID(playerName);
                if (ownerUUID != null) {
                    plugin.getCore().getUuidCache().put(playerName, ownerUUID);
                }
            } catch (NotPremiumException ex) {
                plugin.getLogger().log(Level.FINE, "Username is not premium on refetch", ex);
                plugin.getCore().getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                plugin.getLogger().log(Level.SEVERE, "Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID);
            if (cachedSkin == null) {
                cachedSkin = plugin.getCore().downloadSkin(ownerUUID);
            }

            preferences.setTargetSkin(cachedSkin);
            save(cachedSkin, preferences);
        }
    }

    private void save(final SkinData skin, final UserPreferences preferences) {
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
