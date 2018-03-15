package com.github.games647.changeskin.core.messages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class ForwardMessage implements ChannelMessage {

    private String commandName;
    private String args;
    private boolean isSource;
    private boolean isOP;

    public ForwardMessage(String commandName, String args, boolean isSource, boolean isOP) {
        this.commandName = commandName;
        this.args = args;
        this.isSource = isSource;
        this.isOP = isOP;
    }

    public ForwardMessage() {
        //reading mode
    }

    public String getCommandName() {
        return commandName;
    }

    public String getArgs() {
        return args;
    }

    public boolean isSource() {
        return isSource;
    }

    public boolean isOP() {
        return isOP;
    }

    @Override
    public String getChannelName() {
        return "ForwardCmd";
    }

    @Override
    public void readFrom(ByteArrayDataInput in) {
        commandName = in.readUTF();
        args = in.readUTF();

        isSource = in.readBoolean();
        isOP = in.readBoolean();
    }

    @Override
    public void writeTo(ByteArrayDataOutput out) {
        out.writeUTF(commandName);
        out.writeUTF(args);

        out.writeBoolean(isSource);
        out.writeBoolean(isOP);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "commandName='" + commandName + '\'' +
                ", args='" + args + '\'' +
                ", isSource=" + isSource +
                ", isOP=" + isOP +
                '}';
    }
}
