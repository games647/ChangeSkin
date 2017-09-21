package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.shared.SharedSkinSelect;

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
    protected void scheduleApplyTask(SkinData targetSkin) {
        ProxyServer.getInstance().getScheduler()
                .runAsync(plugin, new SkinUpdater(plugin, receiver, receiver, targetSkin, false, true));
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(receiver, id);
    }
}
