package com.github.games647.changeskin.core.shared.task;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

public abstract class SharedApplier implements Runnable {

    protected final ChangeSkinCore core;

    protected final SkinModel targetSkin;
    protected final boolean keepSkin;

    public SharedApplier(ChangeSkinCore core, SkinModel targetSkin, boolean keepSkin) {
        this.core = core;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    protected abstract boolean isConnected();

    protected abstract void applyInstantUpdate();
    protected abstract void sendMessage(String key);

    protected void applySkin() {
        if (core.getConfig().getBoolean("instantSkinChange")) {
            applyInstantUpdate();
        } else {
            sendMessage("skin-changed-no-instant");
        }
    }

    protected abstract void runAsync(Runnable runnable);

    protected void save(UserPreference preference) {
        if (core.getStorage() == null) {
            return;
        }

        runAsync(() -> {
            //Save the target uuid from the requesting player source
            preference.setTargetSkin(targetSkin);
            preference.setKeepSkin(keepSkin);

            core.getStorage().save(targetSkin);
            core.getStorage().save(preference);
        });
    }
}
