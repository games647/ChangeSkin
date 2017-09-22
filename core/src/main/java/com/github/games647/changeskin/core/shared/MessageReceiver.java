package com.github.games647.changeskin.core.shared;

@FunctionalInterface
public interface MessageReceiver {

    void sendMessageInvoker(String id, String... args);
}
