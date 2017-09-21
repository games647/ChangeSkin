package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.shared.SharedSkinSelect;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;

public class SkinSelector extends SharedSkinSelect {

    private final ChangeSkinSponge plugin;
    private final Player receiver;

    public SkinSelector(ChangeSkinSponge plugin, Player receiver, int targetId) {
        super(plugin.getCore(), targetId);

        this.plugin = plugin;
        this.receiver = receiver;
    }

    @Override
    protected void scheduleApplyTask(SkinData targetSkin) {
        Runnable skinUpdater = new SkinUpdater(plugin, receiver, receiver, targetSkin, true);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(receiver, id);
    }
}
