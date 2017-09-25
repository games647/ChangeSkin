package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.model.GameProfile;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.Optional;
import java.util.UUID;

public abstract class SharedUploader implements Runnable, MessageReceiver {

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
        String oldSkinUrl = core.getAuthApi().getSkinUrl(profile.getName());

        UUID uuid = profile.getId();
        UUID accessToken = CommonUtil.parseId(owner.getAccessToken());

        core.getAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        Optional<SkinData> optNewSkin = core.getSkinApi().downloadSkin(uuid);
        optNewSkin.ifPresent(skinData -> {
            SkinData newSkin = skinData;
            core.getStorage().save(newSkin);

            core.getAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
            sendMessageInvoker("skin-uploaded", owner.getProfile().getName(), "Skin-" + newSkin.getSkinId());
        });
    }
}
