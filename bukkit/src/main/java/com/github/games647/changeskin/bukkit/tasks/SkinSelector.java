package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedSkinSelect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinSelector extends SharedSkinSelect {

    private final ChangeSkinBukkit plugin;
    private final Player receiver;

    public SkinSelector(ChangeSkinBukkit plugin, Player receiver, int targetId) {
        super(plugin.getCore(), targetId);

        this.plugin = plugin;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinModel targetSkin) {
        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, receiver, receiver, targetSkin, true));
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(receiver, id);
    }
}
