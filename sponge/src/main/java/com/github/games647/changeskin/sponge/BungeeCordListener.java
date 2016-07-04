package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.tasks.SkinUpdater;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

public class BungeeCordListener implements RawDataListener {

    private final ChangeSkinSponge plugin;

    public BungeeCordListener(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
        Player player = (Player) connection;
        
        String subchannel = data.readUTF();

        if ("UpdateSkin".equalsIgnoreCase(subchannel)) {
            plugin.getLogger().info("Received instant update request from BungeeCord. "
                    + "This request should only be send if the command /setskin was invoked");
            updateSkin(data, player);
        } else if ("PermissionsCheck".equalsIgnoreCase(subchannel)) {
            checkPermissions(player, data);
        }
    }

    private boolean updateSkin(ChannelBuf data, Player player) throws IllegalArgumentException {
        String encodedData = data.readUTF();
        if (encodedData.equalsIgnoreCase("null")) {
            plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(new SkinUpdater(plugin, null, player, null)).submit(plugin);
            return true;
        }

        String signature = data.readUTF();
        Player receiver = player;
        try {
            String playerName = data.readUTF();
            receiver = plugin.getGame().getServer().getPlayer(playerName).orElseGet(null);
            plugin.getLogger().info("Instant update for {}", playerName);
        } catch (Exception ex) {
            plugin.getLogger().warn("You are using an outdated ChangeSkin spigot version");
        }

        SkinData skinData = new SkinData(encodedData, signature);
        plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(new SkinUpdater(plugin, null, receiver, skinData)).submit(plugin);
        return false;
    }

    private void checkPermissions(Player player, ChannelBuf dataInput) {
//        int skinId = dataInput.readInteger();
//        String encodedData = dataInput.readUTF();
//        String encodedSignature = dataInput.readUTF();
//
//        //continue on success only
//        String receiverUUID = dataInput.readUTF();
//
//        SkinData targetSkin = new SkinData(encodedData, encodedSignature);
//        if (checkBungeePerms(player, UUID.fromString(receiverUUID), targetSkin.getUuid())) {
//            ChannelBuf out = Chann
//            out.writeUTF("PermissionsSuccess");
//            out.writeInteger(skinId);
//            out.writeUTF(encodedData);
//            out.writeUTF(encodedSignature);
//            out.writeUTF(receiverUUID);
//
//            plugin.getPluginChannel().sendTo(player, out);
//        } else {
//            ByteArrayDataOutput out = ByteStreams.newDataOutput();
//            out.writeUTF("PermissionsFailure");
//            player.sendPluginMessage(plugin, plugin.getPluginContainer().getId(), out.toByteArray());
//        }
    }

//    private boolean checkBungeePerms(Player player, UUID receiver, UUID targetSkinUUID) {
//        if (player.getUniqueId().equals(receiver)) {
//            return player.hasPermission(plugin.getPluginContainer().getId() + ".command.setskin")
//                && plugin.checkPermission(player, targetSkinUUID, false);
//        } else {
//            return player.hasPermission(plugin.getPluginContainer().getId() + ".command.setskin.other")
//                    && plugin.checkPermission(player, targetSkinUUID, false);
//        }
//    }
}
