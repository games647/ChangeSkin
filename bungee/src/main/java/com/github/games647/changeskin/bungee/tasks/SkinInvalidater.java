package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinInvalidater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;

    private final boolean bukkitOp;

    public SkinInvalidater(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, boolean bukkitOp) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;

        this.bukkitOp = bukkitOp;
    }

    @Override
    public void run() {
        UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            plugin.sendMessage(invoker, "dont-have-skin");
        } else {
            plugin.sendMessage(invoker, "invalidate-request");

            SkinData skin = plugin.getCore().getMojangSkinApi().downloadSkin(ownedSkin.getUuid());
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
            SkinUpdater skinUpdater = new SkinUpdater(plugin, invoker, receiver, skin, bukkitOp);
            ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUpdater);
        }
    }
}
