package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.craftapi.UUIDAdapter;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.model.auth.Account;
import com.github.games647.craftapi.model.skin.SkinProperty;
import com.github.games647.craftapi.model.skin.Texture;
import com.github.games647.craftapi.model.skin.TextureType;
import com.github.games647.craftapi.resolver.RateLimitException;

import java.io.IOException;
import java.util.Optional;
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
        Profile profile = owner.getProfile();
        String oldSkinUrl = "";
        try {
            Optional<SkinProperty> skinProperty = core.getResolver().downloadSkin(profile.getId());
            if (skinProperty.isPresent()) {
                Optional<Texture> skinTexture = core.getResolver().decodeSkin(skinProperty.get()).getTexture(TextureType.SKIN);
                if (skinTexture.isPresent()) {
                    oldSkinUrl = skinTexture.get().getUrl();
                }
            }
        } catch (IOException | RateLimitException ex) {
            core.getLogger().error("Failed to retrieve old url", ex);
        }

        UUID uuid = profile.getId();
        UUID accessToken = UUIDAdapter.parseId(owner.getAccessToken());

        core.getAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        core.getSkinApi().downloadSkin(uuid).ifPresent(skinData -> {
            core.getStorage().save(skinData);

            core.getAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
            String localeMessage = core.getMessage("skin-uploaded")
                    .replace("{0}", owner.getProfile().getName())
                    .replace("{1}", "Skin-" + skinData.getSkinId());
            sendMessageInvoker(localeMessage);
        });
    }

    protected abstract void sendMessageInvoker(String localeMessage);
}
