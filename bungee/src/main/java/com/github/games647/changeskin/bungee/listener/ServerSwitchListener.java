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
        ServerInfo targetServer = connectEvent.getTarget();

        Server fromServer = connectEvent.getPlayer().getServer();
        if (fromServer != null && Objects.equals(targetServer, fromServer.getInfo())) {
            return;
        }

        ProxiedPlayer player = connectEvent.getPlayer();

        List<String> blacklist = core.getConfig().getStringList("server-blacklist");
        if (blacklist != null && blacklist.contains(targetServer.getName())) {
            //clear the skin
            plugin.applySkin(player, null);
        } else {
            UserPreference session = plugin.getLoginSession(player.getPendingConnection());
            if (session == null) {
                ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> onLazyLoad(player));
            } else {
                SkinModel targetSkin = session.getTargetSkin();
                if (!session.isKeepSkin()) {
                    targetSkin = core.checkAutoUpdate(targetSkin);
                }

                session.setTargetSkin(targetSkin);
                plugin.applySkin(player, targetSkin);
            }
        }
    }

    private void onLazyLoad(ProxiedPlayer player) {
        UserPreference preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.startSession(player.getPendingConnection(), preferences);
        if (preferences.getTargetSkin() == null && core.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(player.getName(), preferences);
            if (preferences.getTargetSkin() == null) {
                //still no skin
                setRandomSkin(preferences, player);
            }
        }

        plugin.applySkin(player, preferences.getTargetSkin());
    }
}
