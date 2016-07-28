package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.google.common.base.Objects;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class SkinDownloader implements Runnable {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;
    private final UUID targetUUID;

    public SkinDownloader(ChangeSkinSponge plugin, CommandSource invoker, Player receiver, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetSkin;
    }

    @Override
    public void run() {
        SkinData skin = plugin.getCore().getStorage().getSkin(targetUUID);
        int updateDiff = plugin.getCore().getAutoUpdateDiff();
        if (skin == null || (updateDiff > 0 && System.currentTimeMillis() - skin.getTimestamp() > updateDiff)) {
            SkinData updatedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(targetUUID);
            if (!Objects.equal(updatedSkin, skin)) {
                skin = updatedSkin;
            }
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        plugin.getGame().getScheduler().createTaskBuilder()
                .execute(new SkinUpdater(plugin, invoker, receiver, skin))
                .submit(plugin);
    }
}
