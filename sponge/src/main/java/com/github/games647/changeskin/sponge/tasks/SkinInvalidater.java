package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.entity.living.player.Player;

public class SkinInvalidater implements Runnable {

    private final ChangeSkinSponge plugin;
    private final Player invoker;

    public SkinInvalidater(ChangeSkinSponge plugin, Player invoker) {
        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void run() {
        UserPreference preferences = plugin.getCore().getStorage().getPreferences(invoker.getUniqueId());
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            plugin.sendMessage(invoker, "dont-have-skin");
        } else {
            plugin.sendMessage(invoker, "invalidate-request");

            SkinData skin = plugin.getCore().getMojangSkinApi().downloadSkin(ownedSkin.getUuid());
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
            plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(new SkinUpdater(plugin, invoker, invoker, skin))
                    .submit(plugin);
        }
    }
}
