package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

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
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            sendMessageInvoker("dont-have-skin");
        } else {
            sendMessageInvoker("invalidate-request");

            Optional<SkinData> skin = core.getMojangSkinApi().downloadSkin(ownedSkin.getUuid());
            if (skin.isPresent()) {
                scheduleApplyTask(skin.get());
            }
        }
    }

    protected abstract void scheduleApplyTask(SkinData skinData);
}
