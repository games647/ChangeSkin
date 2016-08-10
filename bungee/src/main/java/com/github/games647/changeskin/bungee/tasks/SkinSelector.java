package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinSelector implements Runnable {

    private final ChangeSkinBungee plugin;

    private final ProxiedPlayer receiver;
    private final int targetId;

    public SkinSelector(ChangeSkinBungee plugin, ProxiedPlayer receiver, int targetId) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.targetId = targetId;
    }

    @Override
    public void run() {
        SkinData targetSkin = plugin.getStorage().getSkin(targetId);
        if (targetSkin == null) {
            plugin.sendMessage(receiver, "skin-not-found");
        }

        BungeeCord.getInstance().getScheduler()
                .runAsync(plugin, new SkinUpdater(plugin, receiver, receiver, targetSkin, false));
    }
}
