package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.base.Objects;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

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
            try {
                ownerUUID = plugin.getCore().getMojangSkinApi().getUUID(playerName);
            } catch (NotPremiumException ex) {
                plugin.getLogger().log(Level.FINE, "Username is not premium on refetch");
                plugin.getCore().getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                plugin.getLogger().log(Level.SEVERE, "Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            plugin.getCore().getUuidCache().put(playerName, ownerUUID);
            SkinData storedSkin = plugin.getStorage().getSkin(ownerUUID);

            int updateDiff = plugin.getCore().getAutoUpdateDiff();
            if (storedSkin == null
                    || (updateDiff > 0 && System.currentTimeMillis() - storedSkin.getTimestamp() > updateDiff)) {
                SkinData updatedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(ownerUUID);
                if (!Objects.equal(updatedSkin, storedSkin)) {
                    storedSkin = updatedSkin;
                }
            }

            preferences.setTargetSkin(storedSkin);
            save(storedSkin, preferences);
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

                ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> plugin.getStorage().save(preferences));
            }
        }
    }

    public void save(final SkinData skin, final UserPreference preferences) {
        //this can run in the background
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            if (plugin.getStorage().save(skin)) {
                plugin.getStorage().save(preferences);
            }
        });
    }
}
