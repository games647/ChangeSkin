package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;

public abstract class SharedSkinSelect implements Runnable, MessageReceiver {

    protected final ChangeSkinCore core;
    protected final int targetId;

    public SharedSkinSelect(ChangeSkinCore core, int targetId) {
        this.core = core;
        this.targetId = targetId;
    }

    @Override
    public void run() {
        SkinData targetSkin = core.getStorage().getSkin(targetId);
        if (targetSkin == null) {
            sendMessageInvoker("skin-not-found");
            return;
        }

        scheduleApplyTask(targetSkin);
    }

    protected abstract void scheduleApplyTask(SkinData targetSkin);
}
