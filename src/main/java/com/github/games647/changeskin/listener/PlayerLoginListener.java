package com.github.games647.changeskin.listener;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;
import com.github.games647.changeskin.SkinData;
import com.github.games647.changeskin.tasks.NameResolver;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    private static final String SKIN_KEY = "textures";

    private final ChangeSkin plugin;
    private final boolean isSpigot = Bukkit.getServer().getVersion().contains("Spigot");
    private final Random random = new Random();

    public PlayerLoginListener(ChangeSkin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        Player player = loginEvent.getPlayer();

        boolean skinFound = false;

        //try to use the existing and put it in the cache so we use it for others
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        Collection<WrappedSignedProperty> values = properties.get(SKIN_KEY);
        for (WrappedSignedProperty value : values) {
            if (value.hasSignature()) {
                //found a skin
                plugin.getSkinCache().put(player.getUniqueId(), value);
                skinFound = true;
                break;
            }
        }

        //updates to the chosen one
        UUID targetUUID = plugin.getUserPreferences().get(player.getUniqueId());
        if (isSpigot) {
            if (targetUUID != null) {
                WrappedSignedProperty cachedSkin = plugin.getSkinCache().get(targetUUID);
                if (cachedSkin != null) {
                    properties.put(SKIN_KEY, cachedSkin);
                }
            } else if (!skinFound) {
                setRandomSkin(player, properties);
            }
        } else {
            fallbackBukkit(player, targetUUID, properties);
        }
    }

    private void setRandomSkin(Player player, Multimap<String, WrappedSignedProperty> properties) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                plugin.getUserPreferences().put(player.getUniqueId(), targetSkin.getSkinOwner());
                plugin.getSkinCache().put(targetSkin.getSkinOwner(), targetSkin.getSkinValue());
                properties.put(SKIN_KEY, targetSkin.getSkinValue());
            }
        }
    }

    private void fallbackBukkit(Player player, UUID targetUUID, Multimap<String, WrappedSignedProperty> properties) {
        UUID playerUUID = player.getUniqueId();
        if (targetUUID == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                refetch(player, playerUUID);
            } else {
                setRandomSkin(player, properties);
            }
        } else if (!plugin.getSkinCache().containsKey(targetUUID)) {
            //player selected a custom skin which isn't in the cache. Try to download it
            WrappedSignedProperty downloadedSkin = plugin.downloadSkin(targetUUID);
            if (downloadedSkin != null) {
                //run it blocking because we don't know how it takes, so it won't end into a race condition
                plugin.getSkinCache().put(targetUUID, downloadedSkin);
            }
        }
    }

    private void refetch(Player player, UUID playerUUID) {
        if (!plugin.getSkinCache().containsKey(playerUUID)) {
            Bukkit.getScheduler()
                    .runTaskAsynchronously(plugin, new NameResolver(plugin, null, player.getName(), player));
        }
    }
}
