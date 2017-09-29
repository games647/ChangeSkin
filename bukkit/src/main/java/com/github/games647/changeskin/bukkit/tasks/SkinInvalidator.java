package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.shared.SharedInvalidator;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinInvalidator extends SharedInvalidator {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;
    public SkinInvalidator(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver) {
        super(plugin.getCore(), receiver.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }

    @Override
    protected void scheduleApplyTask(SkinData skinData) {
        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, receiver, skinData, false));
    }
}
