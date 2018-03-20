package com.github.games647.changeskin.core.shared.task;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.MessageReceiver;

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
        SkinModel storedSkin = core.getStorage().getSkin(targetUUID);
        if (storedSkin == null) {
            storedSkin = core.getSkinApi().downloadSkin(targetUUID).orElse(null);
        } else {
            storedSkin = core.checkAutoUpdate(storedSkin);
        }

        if (targetUUID.equals(receiverUUID)) {
            sendMessageInvoker("reset");
        }

        scheduleApplyTask(storedSkin);
    }

    protected abstract void scheduleApplyTask(SkinModel skinData);
}
