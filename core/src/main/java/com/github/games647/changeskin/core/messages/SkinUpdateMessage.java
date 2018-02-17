package com.github.games647.changeskin.core.messages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class SkinUpdateMessage implements ChannelMessage {

    private String playerName;

    public SkinUpdateMessage(String playerName) {
        this.playerName = playerName;
    }

    public SkinUpdateMessage() {
        //reading mode
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getChannelName() {
        return "UpdateSkin";
    }

    @Override
    public void readFrom(ByteArrayDataInput in) {
        playerName = in.readUTF();
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeUTF(playerName);
    }
}
