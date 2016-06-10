package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinUpdater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;
    private final SkinData targetSkin;

    public SkinUpdater(ChangeSkinBungee changeSkin, ProxiedPlayer receiver, SkinData targetSkin) {
        this.plugin = changeSkin;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
    }

    @Override
    public void run() {
        if (!receiver.isConnected()) {
            return;
        }

        plugin.applySkin(receiver, targetSkin);
        plugin.sendMessage(receiver, "skin-changed");
    }
}
