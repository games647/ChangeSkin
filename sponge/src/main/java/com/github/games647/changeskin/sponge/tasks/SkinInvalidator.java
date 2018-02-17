package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedInvalidator;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class SkinInvalidator extends SharedInvalidator {

    private final ChangeSkinSponge plugin;
    private final Player invoker;

    public SkinInvalidator(ChangeSkinSponge plugin, Player invoker) {
        super(plugin.getCore(), invoker.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }

    @Override
    protected void scheduleApplyTask(SkinModel skinData) {
        Runnable skinUpdater = new SkinApplier(plugin, invoker, invoker, skinData, false);
        Task.builder().execute(skinUpdater).submit(plugin);
    }
}
