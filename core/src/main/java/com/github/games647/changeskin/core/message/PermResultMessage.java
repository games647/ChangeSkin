package com.github.games647.changeskin.core.message;

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

        int rowId = in.readInt();
        String encodedValue = in.readUTF();
        String encodedSignature = in.readUTF();

        skin = SkinModel.createSkinFromEncoded(encodedValue, encodedSignature);
        skin.setRowId(rowId);

        receiverUUID = UUID.fromString(in.readUTF());
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeBoolean(allowed);

        out.writeInt(skin.getRowId());
        out.writeUTF(skin.getEncodedValue());
        out.writeUTF(skin.getSignature());
        out.writeUTF(receiverUUID.toString());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "allowed=" + allowed +
                ", skin=" + skin +
                ", receiverUUID=" + receiverUUID +
                '}';
    }
}
