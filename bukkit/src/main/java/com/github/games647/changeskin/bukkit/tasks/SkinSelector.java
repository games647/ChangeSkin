package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.SkinData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinSelector implements Runnable {

    private final ChangeSkinBukkit plugin;

    private final Player receiver;
    private final int targetId;

    public SkinSelector(ChangeSkinBukkit plugin, Player receiver, int targetId) {
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

        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, receiver, receiver, targetSkin, true));
    }
}
