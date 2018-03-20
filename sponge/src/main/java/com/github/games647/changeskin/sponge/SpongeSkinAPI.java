package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.shared.ChangeSkinAPI;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;

public class SpongeSkinAPI implements ChangeSkinAPI<Player, GameProfile> {

    @Override
    public void applySkin(Player player, SkinModel targetSkin) {
        applyProperties(player.getProfile(), targetSkin);
    }

    @Override
    public void applyProperties(GameProfile profile, SkinModel targetSkin) {
        //remove existing skins
        profile.getPropertyMap().clear();

        if (targetSkin != null) {
            GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
            ProfileProperty profileProperty = profileManager.createProfileProperty(SkinProperty.SKIN_KEY
                    , targetSkin.getEncodedValue(), targetSkin.getSignature());
            profile.getPropertyMap().put(SkinProperty.SKIN_KEY, profileProperty);
        }
    }
}
