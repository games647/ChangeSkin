package com.github.games647.changeskin.bukkit.bungee;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.message.CheckPermMessage;
import com.github.games647.changeskin.core.message.PermResultMessage;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class CheckPermissionListener implements PluginMessageListener {

    private final ChangeSkinBukkit plugin;

    public CheckPermissionListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        CheckPermMessage message = new CheckPermMessage();
        message.readFrom(dataInput);

        UUID receiverUUID = message.getReceiverUUD();
        boolean op = message.isOp();
        SkinModel targetSkin = message.getTargetSkin();
        UUID skinProfile = targetSkin.getProfileId();

        boolean success = op || checkBungeePerms(player, receiverUUID, message.isSkinPerm(), skinProfile);
        plugin.sendPluginMessage(player, new PermResultMessage(success, targetSkin, receiverUUID));
    }

    private boolean checkBungeePerms(Player player, UUID receiverUUID, boolean skinPerm, UUID targetUUID) {
        if (player.getUniqueId().equals(receiverUUID)) {
            return checkPerm(player, "command.setskin", skinPerm, targetUUID);
        }

        return checkPerm(player, "command.setskin.other", skinPerm, targetUUID);
    }

    private boolean checkPerm(Player invoker, String node, boolean skinPerm, UUID targetUUID) {
        String pluginName = plugin.getName().toLowerCase();
        boolean hasCommandPerm = invoker.hasPermission(pluginName +  '.' + node);
        if (skinPerm) {
            return hasCommandPerm && plugin.hasSkinPermission(invoker, targetUUID, false);
        }

        return hasCommandPerm;
    }
}
