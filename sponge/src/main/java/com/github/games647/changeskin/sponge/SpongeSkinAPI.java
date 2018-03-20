package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.shared.ChangeSkinAPI;
import com.github.games647.changeskin.sponge.task.SkinApplier;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;

public class SpongeSkinAPI implements ChangeSkinAPI<Player, GameProfile> {

    private final ChangeSkinSponge plugin;

    public SpongeSkinAPI(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

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

    @Override
    public void setPersistentSkin(Player player, SkinModel newSkin, boolean applyNow) {
        new SkinApplier(plugin, null, player, newSkin, true).run();
    }

    @Override
    public void setPersistentSkin(Player player, UUID targetSkinId, boolean applyNow) {
        SkinModel newSkin = plugin.getCore().getStorage().getSkin(targetSkinId);
        if (newSkin == null) {
            Optional<SkinModel> downloadSkin = plugin.getCore().getSkinApi().downloadSkin(targetSkinId);
            if (downloadSkin.isPresent()) {
                newSkin = downloadSkin.get();
            }
        }

        setPersistentSkin(player, newSkin, applyNow);
    }
}
