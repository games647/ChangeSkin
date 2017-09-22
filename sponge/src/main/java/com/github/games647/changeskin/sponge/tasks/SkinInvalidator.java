package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.shared.SharedInvalidator;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;

public class SkinInvalidator extends SharedInvalidator {

    private final ChangeSkinSponge plugin;
    private final Player invoker;

    public SkinInvalidator(ChangeSkinSponge plugin, Player invoker) {
        super(plugin.getCore(), invoker.getUniqueId());

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(invoker, id, args);
    }

    @Override
    protected void scheduleApplyTask(SkinData skinData) {
        Runnable skinUpdater = new SkinUpdater(plugin, invoker, invoker, skinData, false);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
    }
}
