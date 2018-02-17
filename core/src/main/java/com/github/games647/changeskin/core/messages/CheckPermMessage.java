package com.github.games647.changeskin.core.messages;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class CheckPermMessage implements ChannelMessage {

    private SkinModel targetSkin;

    private UUID receiverUUD;
    private boolean skinPerm;
    private boolean isOp;

    public CheckPermMessage(SkinModel targetSkin, UUID receiverUUD, boolean skinPerm, boolean isOp) {
        this.targetSkin = targetSkin;
        this.receiverUUD = receiverUUD;
        this.skinPerm = skinPerm;
        this.isOp = isOp;
    }

    public CheckPermMessage() {
        //reading mode
    }

    public SkinModel getTargetSkin() {
        return targetSkin;
    }

    public UUID getReceiverUUD() {
        return receiverUUD;
    }

    public boolean isSkinPerm() {
        return skinPerm;
    }

    public boolean isOp() {
        return isOp;
    }

    @Override
    public String getChannelName() {
        return "PermissionsCheck";
    }

    @Override
    public void readFrom(ByteArrayDataInput in) {
        int skinId = in.readInt();
        String encodedData = in.readUTF();
        String encodedSignature = in.readUTF();

        targetSkin = SkinModel.createSkinFromEncoded(encodedData, encodedSignature);
        targetSkin.setSkinId(skinId);

        //continue on success only
        receiverUUD = UUID.fromString(in.readUTF());
        boolean skinPerm = in.readBoolean();
        boolean isOp = in.readBoolean();
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeInt(targetSkin.getSkinId());
        out.writeUTF(targetSkin.getEncodedValue());
        out.writeUTF(targetSkin.getSignature());

        out.writeUTF(receiverUUD.toString());
    }
}
