package com.github.games647.changeskin.core.shared.task;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.auth.Account;

import java.util.UUID;

public abstract class SharedSkinChanger implements Runnable {

    protected final ChangeSkinCore core;
    protected final Account owner;
    protected final String url;
    protected final String oldSkinUrl;

    public SharedSkinChanger(ChangeSkinCore core, Account owner, String url, String oldSkinUrl) {
        this.core = core;
        this.owner = owner;
        this.url = url;
        this.oldSkinUrl = oldSkinUrl;
    }

    @Override
    public void run() {
        UUID uuid = owner.getProfile().getId();
        String accessToken = owner.getAccessToken();
        core.getAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        core.getSkinApi().downloadSkin(uuid).ifPresent(skinData -> {
            core.getStorage().save(skinData);

            String localeMessage = core.getMessage("skin-uploaded")
                    .replace("{0}", owner.getProfile().getName())
                    .replace("{1}", "Skin-" + skinData.getRowId());
            sendMessageInvoker(localeMessage);

            //revert to the old skin
            core.getAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
        });
    }

    protected abstract void sendMessageInvoker(String localeMessage);
}
