package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        UserPreference session = plugin.getLoginSession(player.getPendingConnection());

        List<String> blacklist = core.getConfig().getStringList("server-blacklist");
        if (blacklist != null && blacklist.contains(targetServer.getName())) {
            //clear the skin
            plugin.getApi().applySkin(player, null);
        } else if (session == null) {
            ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> onLazyLoad(player));
        } else {
            Optional<SkinModel> optSkin = session.getTargetSkin();
            if (optSkin.isPresent()) {
                SkinModel targetSkin = optSkin.get();
                if (!session.isKeepSkin()) {
                    targetSkin = core.checkAutoUpdate(targetSkin);
                }

                session.setTargetSkin(targetSkin);
                plugin.getApi().applySkin(player, targetSkin);
            }
        }
    }

    private void onLazyLoad(ProxiedPlayer player) {
        UserPreference preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.startSession(player.getPendingConnection(), preferences);
        if (!preferences.getTargetSkin().isPresent() && core.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(player.getName(), preferences);
            if (!preferences.getTargetSkin().isPresent()) {
                //still no skin
                setRandomSkin(preferences, player);
            }
        }

        if (preferences.getTargetSkin().isPresent()) {
            plugin.getApi().applySkin(player, preferences.getTargetSkin().get());
        }
    }
}
