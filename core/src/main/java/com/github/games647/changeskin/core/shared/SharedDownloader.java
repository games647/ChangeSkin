package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class SharedDownloader implements Runnable, MessageReceiver {

    protected final ChangeSkinCore core;
    protected final boolean keepSkin;
    protected final UUID targetUUID;

    protected final UUID receiverUUID;

    public SharedDownloader(ChangeSkinCore core, boolean keepSkin, UUID targetUUID, UUID receiverUUID) {
        this.core = core;
        this.keepSkin = keepSkin;
        this.targetUUID = targetUUID;
        this.receiverUUID = receiverUUID;
    }

    @Override
    public void run() {
        SkinData storedSkin = core.getStorage().getSkin(targetUUID);

        int autoUpdateDiff = core.getAutoUpdateDiff();
        if (shouldDownload(storedSkin, autoUpdateDiff)) {
            Optional<SkinData> optUpdatedSkin = core.getMojangSkinApi().downloadSkin(targetUUID);
            if (optUpdatedSkin.isPresent() && !Objects.equals(optUpdatedSkin.get(), storedSkin)) {
                storedSkin = optUpdatedSkin.get();
            }
        }

        if (targetUUID.equals(receiverUUID)) {
            sendMessageInvoker("reset");
        }

        scheduleApplyTask(storedSkin);
    }

    private boolean shouldDownload(SkinData storedSkin, int autoUpdateDiff) {
        if (storedSkin == null) {
            return true;
        }

        return autoUpdateDiff > 0 && System.currentTimeMillis() - storedSkin.getTimestamp() > autoUpdateDiff;
    }

    protected abstract void scheduleApplyTask(SkinData skinData);
}
