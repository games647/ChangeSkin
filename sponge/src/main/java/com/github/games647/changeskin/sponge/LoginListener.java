package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedListener;
import com.google.inject.Inject;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.scheduler.Task;

public class LoginListener extends SharedListener {

    private final ChangeSkinSponge plugin;
    private final Random random = new Random();

    @Inject
    LoginListener(ChangeSkinSponge plugin) {
        super(plugin.getCore());

        this.plugin = plugin;
    }

    @Listener
    public void onPlayerPreLogin(ClientConnectionEvent.Auth preLoginEvent) {
        SkinStorage storage = core.getStorage();
        GameProfile profile = preLoginEvent.getProfile();
        UUID playerUUID = profile.getUniqueId();

        UserPreference preferences = storage.getPreferences(playerUUID);
        SkinModel targetSkin = preferences.getTargetSkin();
        if (targetSkin == null) {
            if (!core.getConfig().getBoolean("restoreSkins")
                    || !refetchSkin(profile.getName().get(), preferences)) {
                setDefaultSkin(preferences, profile);
            }
        } else {
            if (!preferences.isKeepSkin()) {
                targetSkin = core.checkAutoUpdate(targetSkin);
            }

            applySkin(targetSkin, profile);
            save(targetSkin, preferences);
        }
    }

    private void applySkin(SkinModel skinData, GameProfile profile) {
        GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
        ProfileProperty profileProperty = profileManager.createProfileProperty(ChangeSkinCore.SKIN_KEY
                , skinData.getEncodedValue(), skinData.getSignature());
        profile.getPropertyMap().clear();
        profile.getPropertyMap().put(ChangeSkinCore.SKIN_KEY, profileProperty);
    }

    private void setDefaultSkin(UserPreference preferences, GameProfile profile) {
        List<SkinModel> defaultSkins = core.getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());
            SkinModel defaultSkin = defaultSkins.get(randomIndex);
            if (defaultSkin != null) {
                preferences.setTargetSkin(defaultSkin);
                save(defaultSkin, preferences);
                applySkin(defaultSkin, profile);
            }
        }
    }

    @Override
    protected void save(final SkinModel skinData, final UserPreference preferences) {
        Task.builder()
                .async()
                .execute(() -> {
                    if (core.getStorage().save(skinData)) {
                        core.getStorage().save(preferences);
                    }
                }).submit(plugin);
    }
}
