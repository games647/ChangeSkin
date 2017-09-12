package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.Objects;
import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class SkinDownloader implements Runnable {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;
    private final UUID targetUUID;
    private final boolean keepSkin;

    public SkinDownloader(ChangeSkinSponge plugin, CommandSource invoker, Player receiver, UUID targetUUID
            , boolean keepSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetUUID;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        SkinData skin = plugin.getCore().getStorage().getSkin(targetUUID);
        int updateDiff = plugin.getCore().getAutoUpdateDiff();
        if (skin == null || (updateDiff > 0 && System.currentTimeMillis() - skin.getTimestamp() > updateDiff)) {
            SkinData updatedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(targetUUID);
            plugin.cacheSponge(updatedSkin);
            if (!Objects.equals(updatedSkin, skin)) {
                skin = updatedSkin;
            }
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        Runnable skinUpdater = new SkinUpdater(plugin, invoker, receiver, skin, keepSkin);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
    }
}
