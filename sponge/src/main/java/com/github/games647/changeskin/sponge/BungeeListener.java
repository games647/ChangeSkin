package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.sponge.tasks.SkinUpdater;

import java.util.UUID;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.scheduler.Task;

public class BungeeListener implements RawDataListener {

    private final ChangeSkinSponge plugin;
    private final RawDataChannel pluginChannel;

    public BungeeListener(ChangeSkinSponge plugin, RawDataChannel pluginChannel) {
        this.plugin = plugin;
        this.pluginChannel = pluginChannel;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
        Player player = (Player) connection;

        String subChannel = data.readString();

        if ("UpdateSkin".equalsIgnoreCase(subChannel)) {
            plugin.getLog().info("Received instant update request from BungeeCord. "
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
            Task.builder().execute(skinUpdater).submit(plugin);
            return;
        }

        String signature = data.readString();
        String playerName = data.readString();

        Player receiver = Sponge.getServer().getPlayer(playerName).orElse(player);
        plugin.getLog().info("Instant update for {}", playerName);

        SkinModel skinData = SkinModel.createSkinFromEncoded(encodedData, signature);
        Runnable skinUpdater = new SkinUpdater(plugin, null, receiver, skinData, false);
        Task.builder().execute(skinUpdater).submit(plugin);
    }

    private void checkPermissions(Player player, ChannelBuf dataInput) {
        int skinId = dataInput.readInteger();
        String encodedData = dataInput.readString();
        String encodedSignature = dataInput.readString();

        //continue on success only
        String receiverUUID = dataInput.readString();

        SkinModel targetSkin = SkinModel.createSkinFromEncoded(encodedData, encodedSignature);
        if (checkBungeePerms(player, UUID.fromString(receiverUUID), targetSkin.getProfileId())) {
            pluginChannel.sendTo(player, out -> {
                out.writeString("PermissionsSuccess");
                out.writeInteger(skinId);
                out.writeString(encodedData);
                out.writeString(encodedSignature);
                out.writeString(receiverUUID);
            });
        } else {
            pluginChannel.sendTo(player, out -> out.writeString("PermissionsFailure"));
        }
    }

    private boolean checkBungeePerms(Player player, UUID receiver, UUID targetSkinUUID) {
        if (player.getUniqueId().equals(receiver)) {
            return player.hasPermission(PomData.ARTIFACT_ID + ".command.setskin")
                    && plugin.checkPermission(player, targetSkinUUID, false);
        } else {
            return player.hasPermission(PomData.ARTIFACT_ID + ".command.setskin.other")
                    && plugin.checkPermission(player, targetSkinUUID, false);
        }
    }
}
