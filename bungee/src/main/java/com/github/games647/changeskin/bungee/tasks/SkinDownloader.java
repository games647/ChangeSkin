package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinDownloader implements Runnable {

    protected final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;
    private final UUID targetUUID;

    private final boolean bukkitOp;

    public SkinDownloader(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, UUID targetUUID
            , boolean bukkitOp) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetUUID;

        this.bukkitOp = bukkitOp;
    }

    @Override
    public void run() {
        SkinData newSkin = plugin.getStorage().getSkin(targetUUID);
        if (newSkin == null) {
            newSkin = plugin.getCore().getMojangSkinApi().downloadSkin(targetUUID);
            if (newSkin != null) {
                plugin.getCore().getUuidCache().put(newSkin.getName(), newSkin.getUuid());
            }
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        SkinUpdater skinUpdater = new SkinUpdater(plugin, receiver, newSkin, invoker, bukkitOp);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUpdater);
    }
}
