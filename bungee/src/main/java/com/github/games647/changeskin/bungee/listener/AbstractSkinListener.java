package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public abstract class AbstractSkinListener implements Listener {

    protected final ChangeSkinBungee plugin;
    private final Random random = new Random();

    public AbstractSkinListener(ChangeSkinBungee plugin) {
        this.plugin = plugin;
    }

    public void refetch(UserPreference preferences, String playerName) {
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
            } catch (NotPremiumException ex) {
                plugin.getLogger().log(Level.FINE, "Username is not premium on refetch");
                plugin.getCore().getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                plugin.getLogger().log(Level.SEVERE, "Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            plugin.getCore().getUuidCache().put(playerName, ownerUUID);
            SkinData cachedSkin = plugin.getStorage().getSkin(ownerUUID);
            if (cachedSkin == null) {
                cachedSkin = plugin.getCore().downloadSkin(ownerUUID);
            }

            preferences.setTargetSkin(cachedSkin);
            save(cachedSkin, preferences);
        }
    }

    public void setRandomSkin(final UserPreference preferences, ProxiedPlayer player) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getCore().getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            final SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                preferences.setTargetSkin(targetSkin);
                plugin.applySkin(player, targetSkin);

                ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(preferences);
                    }
                });
            }
        }
    }

    public void save(final SkinData skin, final UserPreference preferences) {
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
