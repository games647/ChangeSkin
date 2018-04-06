package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.shared.ChangeSkinAPI;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

public interface PlatformPlugin<C> {

    String getName();

    Path getPluginFolder();

    ChangeSkinAPI<?, ?> getApi();

    LocaleManager<C> getLocaleManager();

    Logger getLog();

    default ThreadFactory getThreadFactory() {
        return null;
    }

    boolean hasSkinPermission(C invoker, UUID uuid, boolean sendMessage);
}
