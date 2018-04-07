package com.github.games647.changeskin.bungee.task;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.task.SharedSkinSelect;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinSelector extends SharedSkinSelect {

    private final ChangeSkinBungee plugin;

    private final ProxiedPlayer receiver;

    public SkinSelector(ChangeSkinBungee plugin, ProxiedPlayer receiver, int targetId) {
        super(plugin.getCore(), targetId);

        this.plugin = plugin;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinModel targetSkin) {
        Runnable task = new SkinApplier(plugin, receiver, receiver, targetSkin, false, true);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(receiver, id);
    }
}
