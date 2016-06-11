package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.SkinData;

import java.util.UUID;

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
        SkinData skin = plugin.getStorage().getSkin(targetUUID);
        if (skin == null) {
            skin = plugin.getCore().downloadSkin(targetUUID);
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        plugin.getServer().getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, receiver, skin));
    }
}
