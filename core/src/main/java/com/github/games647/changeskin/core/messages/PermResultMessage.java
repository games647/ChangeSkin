package com.github.games647.changeskin.core.messages;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class PermResultMessage implements ChannelMessage {

    private boolean allowed;
    private SkinModel skin;
    private UUID receiverUUID;

    public PermResultMessage(boolean success, SkinModel skin, UUID receiverUUID) {
        this.allowed = success;
        this.skin = skin;
        this.receiverUUID = receiverUUID;
    }

    public PermResultMessage() {
        //reading mode
    }

    public boolean isAllowed() {
        return allowed;
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
        allowed = in.readBoolean();

        int skinId = in.readInt();
        String encodedValue = in.readUTF();
        String encodedSignature = in.readUTF();

        skin = SkinModel.createSkinFromEncoded(encodedValue, encodedSignature);
        skin.setSkinId(skinId);

        receiverUUID = UUID.fromString(in.readUTF());
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeBoolean(allowed);

        out.writeInt(skin.getSkinId());
        out.writeUTF(skin.getEncodedValue());
        out.writeUTF(skin.getSignature());
        out.writeUTF(receiverUUID.toString());
    }
}
