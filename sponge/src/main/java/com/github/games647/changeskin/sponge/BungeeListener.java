package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.tasks.SkinUpdater;

import java.util.UUID;
import java.util.logging.Level;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

public class BungeeListener implements RawDataListener {

    private final ChangeSkinSponge plugin;

    public BungeeListener(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
        Player player = (Player) connection;

        String subChannel = data.readString();

        if ("UpdateSkin".equalsIgnoreCase(subChannel)) {
            plugin.getLogger().info("Received instant update request from BungeeCord. "
                    + "This request should only be send if the command /setskin was invoked");
            updateSkin(data, player);
        } else if ("PermissionsCheck".equalsIgnoreCase(subChannel)) {
            checkPermissions(player, data);
        }
    }

    private void updateSkin(ChannelBuf data, Player player) throws IllegalArgumentException {
        String encodedData = data.readString();
        if ("null".equalsIgnoreCase(encodedData)) {
            Runnable skinUpdater = new SkinUpdater(plugin, null, player, null, false);
            plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
            return;
        }

        String signature = data.readString();
        String playerName = data.readString();

        Player receiver = plugin.getGame().getServer().getPlayer(playerName).orElse(player);
        plugin.getLogger().log(Level.INFO, "Instant update for {0}", playerName);

        SkinData skinData = new SkinData(encodedData, signature);
        Runnable skinUpdater = new SkinUpdater(plugin, null, receiver, skinData, false);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
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
