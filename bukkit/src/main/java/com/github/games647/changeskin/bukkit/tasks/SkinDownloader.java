package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.SkinData;
import com.google.common.base.Objects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinDownloader implements Runnable {

    protected final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final UUID targetUUID;

    public SkinDownloader(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetSkin;
    }

    @Override
    public void run() {
        SkinData storedSkin = plugin.getStorage().getSkin(targetUUID);

        int autoUpdateDiff = plugin.getCore().getAutoUpdateDiff();
        if (storedSkin == null
                || (autoUpdateDiff > 0 && System.currentTimeMillis() - storedSkin.getTimestamp() > autoUpdateDiff)) {
            SkinData updatedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(targetUUID);
            if (!Objects.equal(updatedSkin, storedSkin)) {
                storedSkin = updatedSkin;
            }
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, receiver, storedSkin));
    }
}
