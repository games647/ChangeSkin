package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.UUID;
import net.md_5.bungee.api.ChatColor;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkinDownloader implements Runnable {

    protected final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer receiver;
    private final UUID targetUUID;

    public SkinDownloader(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetSkin;
    }

    @Override
    public void run() {
        SkinData skin = plugin.getStorage().getSkin(targetUUID, true);
        if (skin == null) {
            skin = plugin.getCore().downloadSkin(targetUUID);
            if (skin != null) {
                plugin.getStorage().getSkinUUIDCache().put(targetUUID, skin);
            }
        }

        //Save the target uuid from the requesting player source
        final UserPreferences preferences = plugin.getStorage().getPreferences(receiver.getUniqueId(), false);
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

        if (plugin.getConfiguration().getBoolean("instantSkinChange")) {
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new SkinUpdater(plugin, receiver));
        } else if (invoker != null) {
            //if user is online notify the player
            invoker.sendMessage(new ComponentBuilder("Skin was changed. Relogin to see the changes")
                    .color(ChatColor.DARK_GREEN).create());
        }
    }
}
