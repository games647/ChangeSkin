package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedListener;
import com.google.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;

public class LoginListener extends SharedListener {

    private final ChangeSkinSponge plugin;

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

            plugin.applySkin(profile, targetSkin);
            save(preferences);
        }
    }

    private void setDefaultSkin(UserPreference preferences, GameProfile profile) {
        List<SkinModel> defaultSkins = core.getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(defaultSkins.size());
            SkinModel defaultSkin = defaultSkins.get(randomIndex);
            if (defaultSkin != null) {
                preferences.setTargetSkin(defaultSkin);
                save(preferences);
                plugin.applySkin(profile, defaultSkin);
            }
        }
    }

    @Override
    protected void save(final UserPreference preferences) {
        Task.builder()
                .async()
                .execute(() -> {
                    if (core.getStorage().save(preferences.getTargetSkin())) {
                        core.getStorage().save(preferences);
                    }
                }).submit(plugin);
    }
}
