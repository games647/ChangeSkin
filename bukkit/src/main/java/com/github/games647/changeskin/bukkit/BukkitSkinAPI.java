package com.github.games647.changeskin.bukkit;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.bukkit.events.PlayerChangeSkinEvent;
import com.github.games647.changeskin.bukkit.task.SkinApplier;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.shared.ChangeSkinAPI;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitSkinAPI implements ChangeSkinAPI<Player, WrappedGameProfile> {

    private final ChangeSkinBukkit plugin;

    public BukkitSkinAPI(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void applySkin(Player receiver, SkinModel targetSkin) {
        //Calling the event for changing skins
        Bukkit.getPluginManager().callEvent(new PlayerChangeSkinEvent(receiver, targetSkin));
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

    private WrappedSignedProperty convertToProperty(SkinModel skinData) {
        String encodedValue = skinData.getEncodedValue();
        String signature = skinData.getSignature();
        return WrappedSignedProperty.fromValues(SkinProperty.SKIN_KEY, encodedValue, signature);
    }
}
