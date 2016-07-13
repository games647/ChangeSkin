package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
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

    private final boolean bukkitOp;

    public SkinUpdater(ChangeSkinBungee plugin, ProxiedPlayer receiver, SkinData targetSkin, CommandSender invoker
            , boolean bukkitOp) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.invoker = invoker;

        this.bukkitOp = bukkitOp;
    }

    @Override
    public void run() {
        if (!receiver.isConnected()) {
            return;
        }

        plugin.getStorage().save(targetSkin);

        if (invoker instanceof ProxiedPlayer && targetSkin != null
                && plugin.getConfig().getBoolean("bukkit-permissions")) {
            Server server = ((ProxiedPlayer) invoker).getServer();

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PermissionsCheck");

            //serialize it to restore on response message
            out.writeInt(targetSkin.getSkinId());
            out.writeUTF(targetSkin.getEncodedData());
            out.writeUTF(targetSkin.getEncodedSignature());
            out.writeUTF(receiver.getUniqueId().toString());
            out.writeBoolean(bukkitOp);
            server.sendData(plugin.getName(), out.toByteArray());
            return;
        }

        //uuid was successfull resolved, we could now make a cooldown check
        if (invoker instanceof ProxiedPlayer) {
            plugin.getCore().addCooldown(((ProxiedPlayer) invoker).getUniqueId());
        }

        //Save the target uuid from the requesting player source
        final UserPreference preferences = plugin.getStorage().getPreferences(plugin.getOfflineUUID(receiver.getName()));
        preferences.setTargetSkin(targetSkin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getStorage().save(preferences);
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
