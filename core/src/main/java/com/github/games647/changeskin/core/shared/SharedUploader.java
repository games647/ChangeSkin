package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.GameProfile;
import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
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
        String oldSkinUrl = core.getSkinApi().downloadSkin(profile.getId())
                .map(skinModel -> skinModel.getTextures().get(TextureType.SKIN).getUrl())
                .orElse("");

        UUID uuid = profile.getId();
        UUID accessToken = UUIDTypeAdapter.parseId(owner.getAccessToken());

        core.getAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        core.getSkinApi().downloadSkin(uuid).ifPresent(skinData -> {
            core.getStorage().save(skinData);

            core.getAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
            String localeMessage = core.getMessage("skin-uploaded")
                    .replace("{0}", owner.getProfile().getName())
                    .replace("{1}", "Skin-" + skinData.getRowId());
            sendMessageInvoker(localeMessage);
        });
    }

    protected abstract void sendMessageInvoker(String localeMessage);
}
