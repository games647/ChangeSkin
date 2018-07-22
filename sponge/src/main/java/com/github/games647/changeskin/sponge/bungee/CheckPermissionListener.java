package com.github.games647.changeskin.sponge.bungee;

import com.github.games647.changeskin.core.message.CheckPermMessage;
import com.github.games647.changeskin.core.message.PermResultMessage;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.PomData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

import java.util.UUID;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

public class CheckPermissionListener implements RawDataListener {

    @Inject
    private ChangeSkinSponge plugin;

    @Inject
    @ChannelId(PomData.ARTIFACT_ID + ':' + PermResultMessage.PERMISSION_RESULT_CHANNEL)
    private RawDataChannel permissionsResultChannel;

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data.array());
        CheckPermMessage checkMessage = new CheckPermMessage();
        checkMessage.readFrom(dataInput);

        CheckPermMessage message = new CheckPermMessage();
        message.readFrom(dataInput);

        checkPermissions((Player) connection, message);
    }

    private void checkPermissions(Player player, CheckPermMessage permMessage) {
        UUID receiverUUID = permMessage.getReceiverUUD();
        boolean op = permMessage.isOp();
        SkinModel targetSkin = permMessage.getTargetSkin();
        UUID skinProfile = targetSkin.getProfileId();

        boolean success = op || checkBungeePerms(player, receiverUUID, permMessage.isSkinPerm(), skinProfile);
        sendResultMessage(player, new PermResultMessage(success, targetSkin, receiverUUID));
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

    private void sendResultMessage(Player receiver, PermResultMessage resultMessage) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        resultMessage.writeTo(dataOutput);
        permissionsResultChannel.sendTo(receiver, buf -> buf.writeByteArray(dataOutput.toByteArray()));
    }
}
