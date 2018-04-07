package com.github.games647.changeskin.core.shared.task;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.GameProfile;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.model.skin.TextureType;

import java.util.UUID;

public abstract class SharedUploader implements Runnable {

    protected final ChangeSkinCore core;
    protected final Account owner;
    protected final String url;

    public SharedUploader(ChangeSkinCore core, Account owner, String url) {
        this.core = core;
        this.owner = owner;
        this.url = url;
    }

    @Override
    public void run() {
        GameProfile profile = owner.getProfile();
        UUID id = profile.getId();

        String oldSkinUrl = core.getSkinApi().downloadSkin(id)
                .map(skinModel -> skinModel.getTextures().get(TextureType.SKIN).getUrl())
                .orElse("");

        sendMessageInvoker("skin-change-queued");
        scheduleChangeTask(oldSkinUrl);
    }

    protected abstract void sendMessageInvoker(String key);

    protected abstract void scheduleChangeTask(String oldSkinUrl);
}
