package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinUpdater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;

    public SkinUpdater(ChangeSkinBungee changeSkin, ProxiedPlayer receiver) {
        this.plugin = changeSkin;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        if (!receiver.isConnected()) {
            return;
        }

        if (plugin.getStorage() != null) {
            UserPreferences preferences = plugin.getStorage().getPreferences(receiver.getUniqueId(), false);
            SkinData targetSkin = preferences.getTargetSkin();

            plugin.applySkin(receiver, targetSkin);
        }

        plugin.sendMessage(receiver, "skin-changed");
    }
}
