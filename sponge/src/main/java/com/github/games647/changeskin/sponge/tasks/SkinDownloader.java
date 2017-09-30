package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedDownloader;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class SkinDownloader extends SharedDownloader {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;

    public SkinDownloader(ChangeSkinSponge plugin, CommandSource invoker, Player receiver, UUID targetUUID
            , boolean keepSkin) {
        super(plugin.getCore(), keepSkin, targetUUID, receiver.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinModel skinData) {
        Runnable skinUpdater = new SkinUpdater(plugin, invoker, receiver, skinData, keepSkin);
        Task.builder().execute(skinUpdater).submit(plugin);
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }
}
