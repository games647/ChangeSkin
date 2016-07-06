package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ServerSwitchListener extends AbstractSkinListener {

    public ServerSwitchListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent connectEvent) {
        ServerInfo target = connectEvent.getTarget();

        List<String> blacklist = plugin.getConfig().getStringList("server-blacklist");
        if (blacklist != null && !blacklist.contains(target.getName())) {
            final ProxiedPlayer player = connectEvent.getPlayer();
            if (plugin.getLoginSession(player.getPendingConnection()) == null) {
                ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        onLazyLoad(player);
                    }
                });
            }
        }
    }

    private void onLazyLoad(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();

        UserPreference preferences = plugin.getStorage().getPreferences(plugin.getOfflineUUID(player.getName()));
        plugin.startSession(player.getPendingConnection(), preferences);
        if (preferences.getTargetSkin() == null && plugin.getConfig().getBoolean("restoreSkins")) {
            refetch(preferences, player.getName());
            if (preferences.getTargetSkin() == null) {
                //still no skin
                setRandomSkin(preferences, player);
            } else {
                plugin.applySkin(player, preferences.getTargetSkin());
            }
        }
    }
}
