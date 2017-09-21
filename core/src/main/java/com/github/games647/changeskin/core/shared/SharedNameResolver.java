package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;

import java.util.UUID;
import java.util.logging.Level;

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
                uuid = core.getMojangSkinApi().getUUID(targetName);
                if (uuid == null) {
                    sendMessageInvoker("no-resolve");
                } else {
                    core.getUuidCache().put(targetName, uuid);
                }
            } catch (NotPremiumException notPremiumEx) {
                core.getLogger().log(Level.FINE, "Requested not premium", notPremiumEx);
                core.getCrackedNames().put(targetName, new Object());

                sendMessageInvoker("not-premium");
            } catch (RateLimitException rateLimitEx) {
                core.getLogger().log(Level.SEVERE, "UUID Rate Limit reached", rateLimitEx);
                sendMessageInvoker("rate-limit");
            }
        }

        if (uuid != null) {
            sendMessageInvoker("uuid-resolved");
            if (!hasSkinPermission(uuid)) {
                return;
            }

            sendMessageInvoker("skin-downloading");
            scheduleDownloader(uuid);
        }
    }

    protected abstract boolean hasSkinPermission(UUID uuid);
    protected abstract void scheduleDownloader(UUID uuid);
}
