package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.tasks.SkinUpdater;

import java.util.UUID;

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
        
        String subchannel = data.readString();

        if ("UpdateSkin".equalsIgnoreCase(subchannel)) {
            plugin.getLogger().info("Received instant update request from BungeeCord. "
                    + "This request should only be send if the command /setskin was invoked");
            updateSkin(data, player);
        } else if ("PermissionsCheck".equalsIgnoreCase(subchannel)) {
            checkPermissions(player, data);
        }
    }

    private boolean updateSkin(ChannelBuf data, Player player) throws IllegalArgumentException {
        String encodedData = data.readString();
        if ("null".equalsIgnoreCase(encodedData)) {
            Runnable skinUpdater = new SkinUpdater(plugin, null, player, null, false);
            plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
            return true;
        }

        String signature = data.readString();
        Player receiver = player;
        try {
            String playerName = data.readString();
            receiver = plugin.getGame().getServer().getPlayer(playerName).orElseGet(null);
            plugin.getLogger().info("Instant update for {}", playerName);
        } catch (Exception ex) {
            plugin.getLogger().warn("You are using an outdated ChangeSkin spigot version");
        }

        SkinData skinData = new SkinData(encodedData, signature);
        Runnable skinUpdater = new SkinUpdater(plugin, null, receiver, skinData, false);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
        return false;
    }

    private void checkPermissions(Player player, ChannelBuf dataInput) {
        int skinId = dataInput.readInteger();
        String encodedData = dataInput.readString();
        String encodedSignature = dataInput.readString();

        //continue on success only
        String receiverUUID = dataInput.readString();

        SkinData targetSkin = new SkinData(encodedData, encodedSignature);
        if (checkBungeePerms(player, UUID.fromString(receiverUUID), targetSkin.getUuid())) {
            plugin.getPluginChannel().sendTo(player, out -> {
                out.writeString("PermissionsSuccess");
                out.writeInteger(skinId);
                out.writeString(encodedData);
                out.writeString(encodedSignature);
                out.writeString(receiverUUID);
            });
        } else {
            plugin.getPluginChannel().sendTo(player, out -> out.writeString("PermissionsFailure"));
        }
    }

    private boolean checkBungeePerms(Player player, UUID receiver, UUID targetSkinUUID) {
        if (player.getUniqueId().equals(receiver)) {
            return player.hasPermission(plugin.getPluginContainer().getId() + ".command.setskin")
                && plugin.checkPermission(player, targetSkinUUID, false);
        } else {
            return player.hasPermission(plugin.getPluginContainer().getId() + ".command.setskin.other")
                    && plugin.checkPermission(player, targetSkinUUID, false);
        }
    }
}
