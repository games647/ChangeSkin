package com.github.games647.changeskin.core;

import java.nio.file.Path;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

public interface PlatformPlugin<C> {

    String getName();

    Path getPluginFolder();

    Logger getLog();

    void sendMessage(C receiver, String key);

    ThreadFactory getThreadFactory();
}
