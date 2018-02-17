package com.github.games647.changeskin.core.messages;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class PermissionResultMessage implements ChannelMessage {

    private boolean success;
    private SkinModel skin;
    private UUID receiverUUID;

    public PermissionResultMessage(boolean success, SkinModel skin, UUID receiverUUID) {
        this.success = success;
        this.skin = skin;
        this.receiverUUID = receiverUUID;
    }

    public PermissionResultMessage() {
        //reading mode
    }

    public boolean isSuccess() {
        return success;
    }

    public SkinModel getSkin() {
        return skin;
    }

    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    @Override
    public String getChannelName() {
        return "PermissionResult";
    }

    @Override
    public void readFrom(ByteArrayDataInput in) {
        success = in.readBoolean();

        int skinId = in.readInt();
        String encodedValue = in.readUTF();
        String encodedSignature = in.readUTF();

        skin = SkinModel.createSkinFromEncoded(encodedValue, encodedSignature);
        skin.setSkinId(skinId);

        receiverUUID = UUID.fromString(in.readUTF());
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeBoolean(success);

        out.writeInt(skin.getSkinId());
        out.writeUTF(skin.getEncodedValue());
        out.writeUTF(skin.getSignature());
        out.writeUTF(receiverUUID.toString());
    }
}
