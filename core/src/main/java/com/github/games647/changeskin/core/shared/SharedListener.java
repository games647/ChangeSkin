package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public abstract class SharedListener {

    protected final ChangeSkinCore core;

    public SharedListener(ChangeSkinCore core) {
        this.core = core;
    }

    protected boolean refetchSkin(String playerName, UserPreference preferences) {
        UUID ownerUUID = core.getUuidCache().get(playerName);
        if (ownerUUID == null && !core.getCrackedNames().containsKey(playerName)) {
            try {
                Optional<UUID> optUUID = core.getSkinApi().getUUID(playerName);
                if (optUUID.isPresent()) {
                    ownerUUID = optUUID.get();
                }
            } catch (NotPremiumException ex) {
                core.getLogger().log(Level.FINE, "Username is not premium on refetch");
                core.getCrackedNames().put(playerName, new Object());
            } catch (RateLimitException ex) {
                core.getLogger().log(Level.SEVERE, "Rate limit reached on refetch", ex);
            }
        }

        if (ownerUUID != null) {
            core.getUuidCache().put(playerName, ownerUUID);
            SkinData storedSkin = core.checkAutoUpdate(core.getStorage().getSkin(ownerUUID));

            preferences.setTargetSkin(storedSkin);
            save(storedSkin, preferences);
            //todo: verify apply skin
            return true;
        }

        return false;
    }

    protected abstract void save(SkinData skin, UserPreference preferences);
}
