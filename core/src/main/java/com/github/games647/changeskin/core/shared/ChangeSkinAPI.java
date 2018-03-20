package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.model.skin.SkinModel;

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

    //todo: setPersistentSkin

    //todo: setRandomSkin
    //todo: setSkin
    //todo: clearSkin
    //todo: getCurrentSkin
    //todo: hasSkin
}
