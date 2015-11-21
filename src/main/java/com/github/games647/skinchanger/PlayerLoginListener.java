package com.github.games647.skinchanger;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Multimap;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    private static final String SKIN_KEY = "textures";

    private final ChangeSkin plugin;

    public PlayerLoginListener(ChangeSkin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();

        UUID targetUUID = plugin.getUserPreferences().get(player.getUniqueId());
        if (targetUUID != null) {
            WrappedSignedProperty cachedSkin = plugin.getSkinCache().get(player.getUniqueId());
            if (cachedSkin != null) {
                WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
                Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
                properties.put(SKIN_KEY, cachedSkin);
            }
        }
    }
}
