package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinInvalidater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;

    public SkinInvalidater(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            plugin.sendMessage(invoker, "dont-have-skin");
        } else {
            plugin.sendMessage(invoker, "invalidate-request");

            SkinData skin = plugin.getCore().downloadSkin(ownedSkin.getUuid());
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new SkinUpdater(plugin, invoker, receiver, skin));
        }
    }
}
