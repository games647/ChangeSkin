package com.github.games647.changeskin.sponge.task;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.task.SharedSkinSelect;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class SkinSelector extends SharedSkinSelect {

    private final ChangeSkinSponge plugin;
    private final Player receiver;

    public SkinSelector(ChangeSkinSponge plugin, Player receiver, int targetId) {
        super(plugin.getCore(), targetId);

        this.plugin = plugin;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinModel targetSkin) {
        Runnable skinUpdater = new SkinApplier(plugin, receiver, receiver, targetSkin, true);
        Task.builder().execute(skinUpdater).submit(plugin);
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(receiver, id);
    }
}
