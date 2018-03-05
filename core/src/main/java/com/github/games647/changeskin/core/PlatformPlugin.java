package com.github.games647.changeskin.core;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

public interface PlatformPlugin<C> {

    String getName();

    Path getPluginFolder();

    Logger getLog();

    void sendMessage(C receiver, String key);

    default ThreadFactory getThreadFactory() {
        return null;
    }

    boolean hasSkinPermission(C invoker, UUID uuid, boolean sendMessage);
}
