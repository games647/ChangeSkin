package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener extends AbstractSkinListener {

    public LoginListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(LoginEvent loginEvent) {
        if (loginEvent.isCancelled() || !plugin.getConfig().getStringList("server-blacklist").isEmpty()) {
            return;
        }

        PendingConnection connection = loginEvent.getConnection();
        String playerName = connection.getName().toLowerCase();
        
        if (plugin.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(connection, playerName, loginEvent);
        }
    }

    private void refetchSkin(final PendingConnection conn, final String playerName , final AsyncEvent<?> loginEvent) {
        loginEvent.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                UserPreference preferences = plugin.getStorage().getPreferences(plugin.getOfflineUUID(playerName));
                plugin.startSession(conn, preferences);

                SkinData targetSkin = preferences.getTargetSkin();
                int autoUpdateDiff = plugin.getCore().getAutoUpdateDiff();
                if (targetSkin == null) {
                    refetch(preferences, playerName);
                } else if (autoUpdateDiff > 0
                        && System.currentTimeMillis() - targetSkin.getTimestamp() > autoUpdateDiff) {
                    refetch(preferences, playerName);
                }
            } finally {
                loginEvent.completeIntent(plugin);
            }
        });
    }
}
