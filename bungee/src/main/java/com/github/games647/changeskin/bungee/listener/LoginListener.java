package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.UserPreference;

import java.util.UUID;

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
        UUID playerUuid = connection.getUniqueId();
        String playerName = connection.getName();

        UserPreference preferences = plugin.getStorage().getPreferences(playerUuid);
        plugin.startSession(connection, preferences);
        if (preferences.getTargetSkin() == null && plugin.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(preferences, playerName, loginEvent);
        }
    }

    private void refetchSkin(final UserPreference prefereces, final String playerName
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
}
