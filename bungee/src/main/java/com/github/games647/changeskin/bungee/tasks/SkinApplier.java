package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.messages.ChannelMessage;
import com.github.games647.changeskin.core.messages.CheckPermMessage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SkinApplier implements Runnable {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;
    private final SkinModel targetSkin;
    private final CommandSender invoker;

    private final boolean bukkitOp;
    private final boolean keepSkin;

    public SkinApplier(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, SkinModel targetSkin
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

        UUID receiverUUID = receiver.getUniqueId();
        if (invoker instanceof ProxiedPlayer) {
            if (targetSkin != null && plugin.getCore().getConfig().getBoolean("bukkit-permissions")) {
                Server server = ((ProxiedPlayer) invoker).getServer();
                boolean skinPerm = plugin.getCore().getConfig().getBoolean("skinPermission");

                ChannelMessage message = new CheckPermMessage(targetSkin, receiverUUID, skinPerm, bukkitOp);
                plugin.sendPluginMessage(server, message);
                return;
            }

            //uuid was successful resolved, we could now make a cooldown check
            plugin.getCore().addCooldown(((ProxiedPlayer) invoker).getUniqueId());
        }

        //check if that specific player is online
        UserPreference preferences = plugin.getLoginSession(receiver.getPendingConnection());
        if (preferences == null) {
            preferences = plugin.getStorage().getPreferences(receiverUUID);
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
