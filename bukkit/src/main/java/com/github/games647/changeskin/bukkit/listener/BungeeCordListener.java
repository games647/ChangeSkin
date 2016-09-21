package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinUpdater;
import com.github.games647.changeskin.core.model.SkinData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.UUID;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeCordListener implements PluginMessageListener {

    private final ChangeSkinBukkit plugin;

    public BungeeCordListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(plugin.getName())) {
            return;
        }

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subchannel = dataInput.readUTF();

        if ("UpdateSkin".equalsIgnoreCase(subchannel)) {
            plugin.getLogger().log(Level.INFO, "Received instant update request from BungeeCord. "
                    + "This request should only be send if the command /setskin was invoked");
            updateSkin(dataInput, player);
        } else if ("PermissionsCheck".equalsIgnoreCase(subchannel)) {
            checkPermissions(player, dataInput);
        }
    }

    private boolean updateSkin(ByteArrayDataInput dataInput, Player player) throws IllegalArgumentException {
        String encodedData = dataInput.readUTF();
        if (encodedData.equalsIgnoreCase("null")) {
            Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, player, null, false));
            return true;
        }
        
        String signature = dataInput.readUTF();
        String playerName = dataInput.readUTF();
        Player receiver = Bukkit.getPlayerExact(playerName);
        plugin.getLogger().log(Level.INFO, "Instant update for {0}", playerName);

        SkinData skinData = new SkinData(encodedData, signature);
        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, receiver, skinData, false));
        return false;
    }

    private void checkPermissions(Player player, ByteArrayDataInput dataInput) {
        int skinId = dataInput.readInt();
        String encodedData = dataInput.readUTF();
        String encodedSignature = dataInput.readUTF();

        //continue on success only
        String receiverUUID = dataInput.readUTF();
        boolean isOp = dataInput.readBoolean();

        SkinData targetSkin = new SkinData(encodedData, encodedSignature);
        if (isOp || checkBungeePerms(player, UUID.fromString(receiverUUID), targetSkin.getUuid())) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PermissionsSuccess");
            out.writeInt(skinId);
            out.writeUTF(encodedData);
            out.writeUTF(encodedSignature);
            out.writeUTF(receiverUUID);

            player.sendPluginMessage(plugin, plugin.getName(), out.toByteArray());
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PermissionsFailure");
            player.sendPluginMessage(plugin, plugin.getName(), out.toByteArray());
        }
    }

    private boolean checkBungeePerms(Player player, UUID receiverUUID, UUID targetUUID) {
        if (player.getUniqueId().equals(receiverUUID)) {
            return player.hasPermission(plugin.getName().toLowerCase() + ".command.setskin")
                && plugin.checkPermission(player, targetUUID, false);
        } else {
            return player.hasPermission(plugin.getName().toLowerCase() + ".command.setskin.other")
                    && plugin.checkPermission(player, targetUUID, false);
        }
    }
}
