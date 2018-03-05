package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.Optional;
import java.util.UUID;

public abstract class SharedInvalidator implements Runnable, MessageReceiver {

    protected final ChangeSkinCore core;
    protected final UUID receiverUUID;

    public SharedInvalidator(ChangeSkinCore core, UUID receiverUUID) {
        this.core = core;
        this.receiverUUID = receiverUUID;
    }

    @Override
    public void run() {
        UserPreference preferences = core.getStorage().getPreferences(receiverUUID);
        Optional<SkinModel> ownedSkin = preferences.getTargetSkin();
        if (ownedSkin.isPresent()) {
            sendMessageInvoker("invalidate-request");
            core.getSkinApi().downloadSkin(ownedSkin.get().getProfileId()).ifPresent(this::scheduleApplyTask);
        } else {
            sendMessageInvoker("dont-have-skin");
        }
    }

    protected abstract void scheduleApplyTask(SkinModel skinData);
}
