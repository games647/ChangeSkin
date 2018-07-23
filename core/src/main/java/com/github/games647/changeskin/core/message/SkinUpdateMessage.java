package com.github.games647.changeskin.core.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class SkinUpdateMessage implements ChannelMessage {

    public static final String UPDATE_SKIN_CHANNEL = "SkinUp";

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
        return UPDATE_SKIN_CHANNEL;
    }

    @Override
    public void readFrom(ByteArrayDataInput in) {
        playerName = in.readUTF();
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeUTF(playerName);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "playerName='" + playerName + '\'' +
                '}';
    }
}
