package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedListener;
import com.google.inject.Inject;

import java.util.Optional;
import java.util.UUID;

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
        Optional<SkinModel> optSkin = preferences.getTargetSkin();
        if (optSkin.isPresent()) {
            SkinModel targetSkin = optSkin.get();
            if (!preferences.isKeepSkin()) {
                targetSkin = core.checkAutoUpdate(targetSkin);
            }

            plugin.getApi().applyProperties(profile, targetSkin);
            save(preferences);
        } else {
            String playerName = profile.getName().get();
            if (!core.getConfig().getBoolean("restoreSkins") || !refetchSkin(playerName, preferences)) {
                setDefaultSkin(preferences, profile);
            }
        }
    }

    private void setDefaultSkin(UserPreference preferences, GameProfile profile) {
        Optional<SkinModel> randomSkin = getRandomSkin();
        if (randomSkin.isPresent()) {
            SkinModel targetSkin = randomSkin.get();
            preferences.setTargetSkin(targetSkin);
            plugin.getApi().applyProperties(profile, targetSkin);
        }
    }

    @Override
    protected void save(final UserPreference preferences) {
        Task.builder()
                .async()
                .execute(() -> {
                    Optional<SkinModel> optSkin = preferences.getTargetSkin();
                    if (optSkin.isPresent()) {
                        if (core.getStorage().save(optSkin.get())) {
                            core.getStorage().save(preferences);
                        }
                    } else {
                        core.getStorage().save(preferences);
                    }
                }).submit(plugin);
    }
}
