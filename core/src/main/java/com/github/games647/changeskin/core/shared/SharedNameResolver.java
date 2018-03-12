package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;

import java.util.Optional;
import java.util.UUID;

public abstract class SharedNameResolver implements Runnable, MessageReceiver {

    protected final ChangeSkinCore core;

    protected final String targetName;
    protected final boolean keepSkin;

    public SharedNameResolver(ChangeSkinCore core, String targetName, boolean keepSkin) {
        this.core = core;
        this.targetName = targetName;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        UUID uuid = core.getUuidCache().get(targetName);
        if (uuid == null) {
            if (core.getCrackedNames().containsKey(targetName)) {
                sendMessageInvoker("not-premium");
                return;
            }

            try {
                Optional<UUID> optUUID = core.getSkinApi().getUUID(targetName);
                if (optUUID.isPresent()) {
                    uuid = optUUID.get();
                    core.getUuidCache().put(targetName, uuid);
                } else {
                    sendMessageInvoker("no-resolve");
                }
            } catch (NotPremiumException notPremiumEx) {
                core.getCrackedNames().put(targetName, new Object());
                sendMessageInvoker("not-premium");
            } catch (RateLimitException rateLimitEx) {
                sendMessageInvoker("rate-limit");
            }
        }

        if (uuid != null) {
            sendMessageInvoker("uuid-resolved");
            if (!hasSkinPermission(uuid)) {
                return;
            }

            scheduleDownloader(uuid);
        }
    }

    protected abstract boolean hasSkinPermission(UUID uuid);
    protected abstract void scheduleDownloader(UUID uuid);
}
