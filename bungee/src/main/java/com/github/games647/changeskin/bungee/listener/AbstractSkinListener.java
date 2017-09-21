package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.shared.SharedListener;

import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public abstract class AbstractSkinListener extends SharedListener implements Listener {

    protected final ChangeSkinBungee plugin;
    private final Random random = new Random();

    public AbstractSkinListener(ChangeSkinBungee plugin) {
        super(plugin.getCore());

        this.plugin = plugin;
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

    @Override
    public void save(final SkinData skin, final UserPreference preferences) {
        //this can run in the background
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            if (plugin.getStorage().save(skin)) {
                plugin.getStorage().save(preferences);
            }
        });
    }
}
