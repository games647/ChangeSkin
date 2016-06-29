package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SkinUpdater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;
    private final SkinData targetSkin;
    private final CommandSender invoker;

    public SkinUpdater(ChangeSkinBungee changeSkin, CommandSender invoker, ProxiedPlayer receiver, SkinData targetSkin) {
        this.plugin = changeSkin;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.invoker = invoker;
    }

    @Override
    public void run() {
        if (!receiver.isConnected()) {
            return;
        }

        if (invoker instanceof ProxiedPlayer && plugin.getConfig().getBoolean("bukkit-permissions")) {
            Server server = ((ProxiedPlayer) invoker).getServer();

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PermissionsCheck");

            //serialize it to restore on response message
            out.writeInt(targetSkin.getSkinId());
            out.writeUTF(targetSkin.getEncodedData());
            out.writeUTF(targetSkin.getEncodedSignature());
            out.writeUTF(receiver.getUniqueId().toString());
            server.sendData(plugin.getName(), out.toByteArray());
            return;
        }

        //uuid was successfull resolved, we could now make a cooldown check
        if (invoker instanceof ProxiedPlayer) {
            plugin.addCooldown(((ProxiedPlayer) invoker).getUniqueId());
        }

        //Save the target uuid from the requesting player source
        final UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        preferences.setTargetSkin(targetSkin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                if (plugin.getStorage().save(targetSkin)) {
                    plugin.getStorage().save(preferences);
                }
            }
        });

        if (plugin.getConfig().getBoolean("instantSkinChange")) {
            plugin.applySkin(receiver, targetSkin);
            plugin.sendMessage(receiver, "skin-changed");
        } else if (invoker != null) {
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }
}
