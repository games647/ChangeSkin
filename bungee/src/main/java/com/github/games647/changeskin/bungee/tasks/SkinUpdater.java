package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SkinUpdater implements Runnable {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;
    private final SkinModel targetSkin;
    private final CommandSender invoker;

    private final boolean bukkitOp;
    private final boolean keepSkin;

    public SkinUpdater(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, SkinModel targetSkin
            , boolean bukkitOp, boolean keepSkin) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.invoker = invoker;

        this.bukkitOp = bukkitOp;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        if (!receiver.isConnected()) {
            return;
        }

        if (invoker instanceof ProxiedPlayer) {
            if (targetSkin != null && plugin.getCore().getConfig().getBoolean("bukkit-permissions")) {
                Server server = ((ProxiedPlayer) invoker).getServer();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("PermissionsCheck");

                //serialize it to restore on response message
                out.writeInt(targetSkin.getSkinId());
                out.writeUTF(targetSkin.getEncodedValue());
                out.writeUTF(targetSkin.getSignature());

                out.writeUTF(receiver.getUniqueId().toString());
                out.writeBoolean(plugin.getCore().getConfig().getBoolean("skinPermission"));
                out.writeBoolean(bukkitOp);

                server.sendData(plugin.getName(), out.toByteArray());
                return;
            }

            //uuid was successful resolved, we could now make a cooldown check
            plugin.getCore().addCooldown(((ProxiedPlayer) invoker).getUniqueId());
        }

        //check if that specific player is online
        UserPreference preferences = plugin.getLoginSession(receiver.getPendingConnection());
        if (preferences == null) {
            plugin.getStorage().getPreferences(receiver.getUniqueId());
        }

        //Save the target uuid from the requesting player source
        preferences.setTargetSkin(targetSkin);
        preferences.setKeepSkin(keepSkin);

        plugin.getStorage().save(targetSkin);
        plugin.getStorage().save(preferences);

        if (plugin.getCore().getConfig().getBoolean("instantSkinChange")) {
            plugin.applySkin(receiver, targetSkin);
        } else if (invoker != null) {
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }
}
