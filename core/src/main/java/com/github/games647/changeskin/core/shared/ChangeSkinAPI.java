package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.UUID;

/**
 * ChangeSkin API
 *
 * @param <P> platform specific player class
 * @param <W> platform specific game profile class
 */
public interface ChangeSkinAPI<P, W> {

    void applySkin(P player, SkinModel targetSkin);

    void applyProperties(W profile, SkinModel targetSkin);

    //todo: refresh (instant update) player

    void setPersistentSkin(P player, SkinModel newSkin, boolean applyNow);

    void setPersistentSkin(P player, UUID newSkin, boolean applyNow);

    //todo: convert SkinModel to property model and vice versa

    //todo: setRandomSkin
    //todo: setSkin
    //todo: clearSkin
    //todo: getCurrentSkin
    //todo: hasSkin
}
