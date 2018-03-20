package com.github.games647.changeskin.bukkit;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.shared.ChangeSkinAPI;

import org.bukkit.entity.Player;

public class BukkitSkinAPI implements ChangeSkinAPI<Player, WrappedGameProfile> {

    @Override
    public void applySkin(Player receiver, SkinModel targetSkin) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(receiver);
        applyProperties(gameProfile, targetSkin);
    }

    @Override
    public void applyProperties(WrappedGameProfile profile, SkinModel targetSkin) {
        //remove existing skins
        profile.getProperties().clear();
        if (targetSkin != null) {
            profile.getProperties().put(SkinProperty.SKIN_KEY, convertToProperty(targetSkin));
        }
    }

    private WrappedSignedProperty convertToProperty(SkinModel skinData) {
        String encodedValue = skinData.getEncodedValue();
        String signature = skinData.getSignature();
        return WrappedSignedProperty.fromValues(SkinProperty.SKIN_KEY, encodedValue, signature);
    }
}
