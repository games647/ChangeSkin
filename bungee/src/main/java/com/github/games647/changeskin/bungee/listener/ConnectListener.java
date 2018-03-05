package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.Optional;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ConnectListener extends AbstractSkinListener {

    public ConnectListener(ChangeSkinBungee plugin) {
        super(plugin);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPostLogin(LoginEvent loginEvent) {
        if (loginEvent.isCancelled() || !core.getConfig().getStringList("server-blacklist").isEmpty()) {
            return;
        }

        PendingConnection connection = loginEvent.getConnection();
        String playerName = connection.getName().toLowerCase();

        if (core.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(connection, playerName, loginEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PostLoginEvent postLoginEvent) {
        ProxiedPlayer player = postLoginEvent.getPlayer();

        //updates to the chosen one
        UserPreference preferences = plugin.getLoginSession(player.getPendingConnection());
        if (preferences == null) {
            return;
        }

        Optional<SkinModel> optSkin = preferences.getTargetSkin();
        if (!optSkin.isPresent()) {
            setRandomSkin(preferences, player);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        PendingConnection pendingConnection = disconnectEvent.getPlayer().getPendingConnection();
        UserPreference preference = plugin.endSession(pendingConnection);

        if (preference != null) {
            save(preference);
        }
    }

    private void refetchSkin(final PendingConnection conn, final String playerName, final AsyncEvent<?> loginEvent) {
        loginEvent.registerIntent(plugin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                UserPreference preferences = plugin.getStorage().getPreferences(conn.getUniqueId());
                plugin.startSession(conn, preferences);

                Optional<SkinModel> optSkin = preferences.getTargetSkin();
                if (optSkin.isPresent()) {
                    SkinModel targetSkin = optSkin.get();
                    if (!preferences.isKeepSkin()) {
                        targetSkin = core.checkAutoUpdate(targetSkin);
                    }

                    preferences.setTargetSkin(targetSkin);
                } else {
                    refetchSkin(playerName, preferences);
                }
            } finally {
                loginEvent.completeIntent(plugin);
            }
        });
    }
}
