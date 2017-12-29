package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.List;
import java.util.Objects;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
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

        Server fromServer = connectEvent.getPlayer().getServer();
        if (fromServer != null && Objects.equals(target, fromServer.getInfo())) {
            return;
        }

        List<String> blacklist = plugin.getCore().getConfig().getStringList("server-blacklist");
        if (blacklist != null) {
            final ProxiedPlayer player = connectEvent.getPlayer();
            if (blacklist.contains(target.getName())) {
                //clear the skin
                plugin.applySkin(player, null);
            } else {
                UserPreference session = plugin.getLoginSession(player.getPendingConnection());
                if (session == null) {
                    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> onLazyLoad(player));
                } else {
                    SkinModel targetSkin = session.getTargetSkin();
                    if (!session.isKeepSkin()) {
                        targetSkin = plugin.getCore().checkAutoUpdate(targetSkin);
                    }

                    plugin.applySkin(player, targetSkin);
                    SkinModel finalTargetSkin = targetSkin;
                    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> save(finalTargetSkin, session));
                }
            }
        }
    }

    private void onLazyLoad(ProxiedPlayer player) {
        UserPreference preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.startSession(player.getPendingConnection(), preferences);
        if (preferences.getTargetSkin() == null && plugin.getCore().getConfig().getBoolean("restoreSkins")) {
            refetchSkin(player.getName(), preferences);
            if (preferences.getTargetSkin() == null) {
                //still no skin
                setRandomSkin(preferences, player);
            } else {
                plugin.applySkin(player, preferences.getTargetSkin());
            }
        } else if (preferences.getTargetSkin() != null) {
            plugin.applySkin(player, preferences.getTargetSkin());
        }
    }
}
