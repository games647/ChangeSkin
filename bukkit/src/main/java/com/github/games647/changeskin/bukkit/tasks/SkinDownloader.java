package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedDownloader;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinDownloader extends SharedDownloader {

    protected final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;

    public SkinDownloader(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver, UUID targetUUID
            , boolean keepSkin) {
        super(plugin.getCore(), keepSkin, targetUUID, receiver.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinModel skinData) {
        Bukkit.getScheduler().runTask(plugin, new SkinApplier(plugin, invoker, receiver, skinData, keepSkin));
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }
}
