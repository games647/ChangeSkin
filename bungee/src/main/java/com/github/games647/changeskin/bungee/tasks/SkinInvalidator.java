package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedInvalidator;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinInvalidator extends SharedInvalidator {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;

    private final boolean bukkitOp;

    public SkinInvalidator(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, boolean bukkitOp) {
        super(plugin.getCore(), receiver.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.bukkitOp = bukkitOp;
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }

    @Override
    protected void scheduleApplyTask(SkinModel skinData) {
        Runnable skinUpdater = new SkinApplier(plugin, invoker, receiver, skinData, bukkitOp, false);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUpdater);
    }
}
