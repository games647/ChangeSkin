package com.github.games647.changeskin.core;

import java.io.File;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

public interface PlatformPlugin<C> {

    String getName();

    File getDataFolder();

    Logger getLog();

    void sendMessage(C receiver, String message);

    ThreadFactory getThreadFactory();
}
