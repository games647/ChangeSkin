package com.github.games647.changeskin.core;

import java.io.File;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public interface PlatformPlugin<C> {

    String getName();

    File getDataFolder();

    Logger getLogger();

    void sendMessage(C receiver, String message);

    ThreadFactory getThreadFactory();
}
