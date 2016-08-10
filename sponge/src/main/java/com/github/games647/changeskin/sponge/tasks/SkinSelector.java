package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;

public class SkinSelector implements Runnable {

    private final ChangeSkinSponge plugin;

    private final Player receiver;
    private final int targetId;

    public SkinSelector(ChangeSkinSponge plugin, Player receiver, int targetId) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.targetId = targetId;
    }

    @Override
    public void run() {
        SkinData targetSkin = plugin.getCore().getStorage().getSkin(targetId);
        if (targetSkin == null) {
            plugin.sendMessage(receiver, "skin-not-found");
        }

        plugin.getGame().getScheduler().createTaskBuilder()
                .execute(new SkinUpdater(plugin, receiver, receiver, targetSkin))
                .submit(plugin);
    }
}
