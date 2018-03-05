package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedListener;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public abstract class AbstractSkinListener extends SharedListener implements Listener {

    protected final ChangeSkinBungee plugin;

    public AbstractSkinListener(ChangeSkinBungee plugin) {
        super(plugin.getCore());

        this.plugin = plugin;
    }

    public void setRandomSkin(final UserPreference preferences, ProxiedPlayer player) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinModel> defaultSkins = core.getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(defaultSkins.size());

            final SkinModel targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                preferences.setTargetSkin(targetSkin);
            }
        }
    }

    @Override
    public void save(final UserPreference preferences) {
        //this can run in the background
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            Optional<SkinModel> optSkin = preferences.getTargetSkin();
            if (optSkin.isPresent()) {
                if (core.getStorage().save(optSkin.get())) {
                    core.getStorage().save(preferences);
                }
            } else {
                core.getStorage().save(preferences);
            }
        });
    }
}
