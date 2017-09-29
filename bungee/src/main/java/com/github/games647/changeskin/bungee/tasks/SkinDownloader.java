package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.shared.SharedDownloader;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinDownloader extends SharedDownloader {

    protected final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;

    private final boolean bukkitOp;

    public SkinDownloader(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, UUID targetUUID
            , boolean bukkitOp, boolean keepSkin) {
        super(plugin.getCore(), keepSkin, targetUUID, receiver.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;

        this.bukkitOp = bukkitOp;
    }

    @Override
    protected void scheduleApplyTask(SkinData skinData) {
        Runnable skinUpdater = new SkinUpdater(plugin, invoker, receiver, skinData, bukkitOp, keepSkin);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUpdater);
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }
}
