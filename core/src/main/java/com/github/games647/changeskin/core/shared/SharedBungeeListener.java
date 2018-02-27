package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.messages.ChannelMessage;
import com.github.games647.changeskin.core.messages.CheckPermMessage;
import com.github.games647.changeskin.core.messages.PermissionResultMessage;
import com.github.games647.changeskin.core.messages.SkinUpdateMessage;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

public abstract class SharedBungeeListener<P> {

    protected final PlatformPlugin<?> plugin;
    protected final String channelName;

    public SharedBungeeListener(PlatformPlugin<?> plugin) {
        this.plugin = plugin;
        this.channelName = plugin.getName();
    }

    protected void handlePayload(P player, byte[] data) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        String subChannel = dataInput.readUTF();

        if ("UpdateSkin".equalsIgnoreCase(subChannel)) {
            updateSkin(dataInput);
        } else if ("PermissionsCheck".equalsIgnoreCase(subChannel)) {
            checkPermissions(player, dataInput);
        }
    }

    private void updateSkin(ByteArrayDataInput dataInput) throws IllegalArgumentException {
        SkinUpdateMessage message = new SkinUpdateMessage();
        message.readFrom(dataInput);

        String playerName = message.getPlayerName();
        P receiver = getPlayerExact(playerName);

        plugin.getLog().info("Instant update for {}", playerName);
        runUpdater(receiver, null);
    }

    private void checkPermissions(P player, ByteArrayDataInput dataInput) {
        CheckPermMessage message = new CheckPermMessage();
        message.readFrom(dataInput);

        UUID receiverUUID = message.getReceiverUUD();
        boolean op = message.isOp();
        SkinModel targetSkin = message.getTargetSkin();
        UUID skinProfile = targetSkin.getProfileId();

        boolean success = op || checkBungeePerms(player, receiverUUID, skinProfile, message.isSkinPerm());
        sendMessage(player, new PermissionResultMessage(success, targetSkin, receiverUUID));
    }

    private boolean checkBungeePerms(P player, UUID receiverUUID, UUID targetUUID, boolean skinPerm) {
        String pluginName = plugin.getName().toLowerCase();
        if (getUUID(player).equals(receiverUUID)) {
            boolean hasCommandPerm = hasPermission(player, pluginName + ".command.setskin");
            if (skinPerm) {
                return hasCommandPerm && checkWhitelistPermission(player, targetUUID);
            } else {
                return hasCommandPerm;
            }
        } else {
            boolean hasCommandPerm = hasPermission(player, pluginName + ".command.setskin.other");
            if (skinPerm) {
                return hasCommandPerm && checkWhitelistPermission(player, targetUUID);
            } else {
                return hasCommandPerm;
            }
        }
    }

    protected abstract void sendMessage(P player, String channel, byte[] data);

    protected void sendMessage(P player, ChannelMessage message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(message.getChannelName());

        message.writeTo(out);
        sendMessage(player, channelName, out.toByteArray());
    }

    protected abstract void runUpdater(P receiver, SkinModel targetSkin);

    protected abstract P getPlayerExact(String name);

    protected abstract UUID getUUID(P player);

    protected abstract boolean hasPermission(P player, String permission);

    protected abstract boolean checkWhitelistPermission(P player, UUID targetUUID);
}
