package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;

public class LoginListener {

    private final ChangeSkinSponge plugin;

    public LoginListener(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerPreLogin(ClientConnectionEvent.Auth preLoginEvent) {
        SkinStorage storage = plugin.getCore().getStorage();
        GameProfile profile = preLoginEvent.getProfile();
        UUID playerUUID = profile.getUniqueId();

        UserPreferences preferences = storage.getPreferences(playerUUID);
        if (preferences.getTargetSkin() == null) {
            SkinData skinData = refetch(preferences, profile);
            if (skinData != null) {
                //set the skin
                GameProfileManager profileManager = plugin.getGame().getServer().getGameProfileManager();
                ProfileProperty profileProperty = profileManager.createProfileProperty(ChangeSkinCore.SKIN_KEY
                        , skinData.getEncodedData(), skinData.getEncodedSignature());
                profile.getPropertyMap().put(ChangeSkinCore.SKIN_KEY, profileProperty);
            }
        }
    }

    private SkinData refetch(final UserPreferences preferences, GameProfile profile) {
        String playerName = profile.getName().get();
        UUID ownerUUID = plugin.getCore().getUuidCache().get(playerName);

        if (ownerUUID == null && !plugin.getCore().getCrackedNames().containsKey(playerName)) {
            SkinData skin = plugin.getCore().getStorage().getSkin(playerName);
            if (skin != null) {
                plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
                preferences.setTargetSkin(skin);
                save(skin, preferences);
                return skin;
            }

            try {
                ownerUUID = plugin.getCore().getUUID(playerName);
                if (ownerUUID != null) {
                    plugin.getCore().getUuidCache().put(playerName, ownerUUID);
                }
            } catch (NotPremiumException ex) {
                plugin.getLogger().debug("User is not premium", ex);
                plugin.getCore().getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                plugin.getLogger().error("Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            SkinData storedSkin = plugin.getCore().getStorage().getSkin(ownerUUID);
            if (storedSkin == null) {
                storedSkin = plugin.getCore().downloadSkin(ownerUUID);
            }

            preferences.setTargetSkin(storedSkin);
            save(storedSkin, preferences);
            return storedSkin;
        }

        return null;
    }

    private void save(final SkinData skinData, final UserPreferences preferences) {
        plugin.getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(() -> {
                    if (plugin.getCore().getStorage().save(skinData)) {
                        plugin.getCore().getStorage().save(preferences);
                    }
                }).submit(plugin);
    }
}
