package com.github.games647.changeskin.core.messages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public interface ChannelMessage {

    String getChannelName();

    void readFrom(ByteArrayDataInput in);

    void writeTo(ByteArrayDataOutput out);
}
