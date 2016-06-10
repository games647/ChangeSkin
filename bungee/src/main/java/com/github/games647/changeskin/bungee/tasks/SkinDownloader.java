package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinDownloader implements Runnable {

    protected final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;
    private final UUID targetUUID;

    public SkinDownloader(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, UUID targetUUID) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetUUID;
    }

    @Override
    public void run() {
        SkinData skin = plugin.getStorage().getSkin(targetUUID);
        if (skin == null) {
            skin = plugin.getCore().downloadSkin(targetUUID);
        }

        //uuid was successfull resolved, we could now make a cooldown check
        if (invoker instanceof ProxiedPlayer) {
            plugin.addCooldown(((ProxiedPlayer) invoker).getUniqueId());
        }

        //Save the target uuid from the requesting player source
        final UserPreferences preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        preferences.setTargetSkin(skin);

        final SkinData newSkin = skin;
        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                if (plugin.getStorage().save(newSkin)) {
                    plugin.getStorage().save(preferences);
                }
            }
        });

        if (targetUUID.equals(receiver.getUniqueId())) {
            plugin.sendMessage(invoker, "reset");
        }

        if (plugin.getConfiguration().getBoolean("instantSkinChange")) {
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new SkinUpdater(plugin, receiver, newSkin));
        } else if (invoker != null) {
            //if user is online notify the player
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }
}
