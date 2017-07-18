package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.google.common.base.Objects;

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
    private final boolean keepSkin;

    public SkinDownloader(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, UUID targetUUID
            , boolean bukkitOp, boolean keepSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetUUID;

        this.bukkitOp = bukkitOp;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        SkinData targetSkin = plugin.getStorage().getSkin(targetUUID);
        int updateDiff = plugin.getCore().getAutoUpdateDiff();
        long now = System.currentTimeMillis();
        if (targetSkin == null || (updateDiff > 0 && now - targetSkin.getTimestamp() > updateDiff)) {
            SkinData updatedSkin = plugin.getCore().getMojangSkinApi().downloadSkin(targetUUID);
            if (!Objects.equal(updatedSkin, targetSkin)) {
                targetSkin = updatedSkin;
            }
        }

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        Runnable skinUpdater = new SkinUpdater(plugin, invoker, receiver, targetSkin, bukkitOp, keepSkin);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUpdater);
    }
}
