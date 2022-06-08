package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.bungee.task.SkinApplier;
import com.github.games647.changeskin.core.message.SkinUpdateMessage;
import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.shared.ChangeSkinAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BungeeSkinAPI implements ChangeSkinAPI<ProxiedPlayer, LoginResult> {

    //speed by letting the JVM optimize this
    //MethodHandle is only faster for static final fields
    private static final MethodHandle profileSetter;
    private static final Property[] emptyProperties = {};

    static {
        MethodHandle methodHandle = null;
        try {
            Field profileField = InitialHandler.class.getDeclaredField("loginProfile");
            profileField.setAccessible(true);

            methodHandle = MethodHandles.lookup().unreflectSetter(profileField);
        } catch (Exception ex) {
            Logger logger = LoggerFactory.getLogger("ChangeSkin");
            logger.info("Cannot find loginProfile field for setting skin in offline mode", ex);
        }

        profileSetter = methodHandle;
    }

    private final ChangeSkinBungee plugin;

    public BungeeSkinAPI(ChangeSkinBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void applySkin(ProxiedPlayer player, SkinModel targetSkin) {
        plugin.getLog().debug("Applying skin for {}", player.getName());

        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();

        // this is null in offline mode
        if (loginProfile == null) {
            String mojangUUID = UUIDTypeAdapter.toMojangId(player.getUniqueId());

            if (profileSetter != null) {
                try {
                    LoginResult loginResult = new LoginResult(mojangUUID, player.getName(), toProperties(targetSkin));
                    profileSetter.invokeExact(initialHandler, loginResult);
                } catch (Error error) {
                    // rethrow errors we shouldn't silence them like OutOfMemoryError
                    throw error;
                } catch (Throwable throwable) {
                    plugin.getLog().error("Error applying skin: {} for {}", targetSkin, player, throwable);
                }
            }
        } else {
            applyProperties(loginProfile, targetSkin);
        }

        //send plugin channel update request
        plugin.sendPluginMessage(player.getServer(), new SkinUpdateMessage(player.getName()));
    }

    @Override
    public void applyProperties(LoginResult profile, SkinModel targetSkin) {
        Property[] properties = toProperties(targetSkin);
        profile.setProperties(properties);
    }

    @Override
    public void setPersistentSkin(ProxiedPlayer player, SkinModel newSkin, boolean applyNow) {
        new SkinApplier(plugin, player, player, newSkin, false, false).run();
    }

    @Override
    public void setPersistentSkin(ProxiedPlayer player, UUID targetSkinId, boolean applyNow) {
        SkinModel newSkin = plugin.getStorage().getSkin(targetSkinId);
        if (newSkin == null) {
            Optional<SkinModel> downloadSkin = plugin.getCore().getSkinApi().downloadSkin(targetSkinId);
            if (downloadSkin.isPresent()) {
                newSkin = downloadSkin.get();
            }
        }

        setPersistentSkin(player, newSkin, applyNow);
    }

    private Property[] toProperties(SkinModel targetSkin) {
        if (targetSkin == null) {
            return emptyProperties;
        }

        String encodedValue = targetSkin.getEncodedValue();
        String signature = targetSkin.getSignature();
        Property prop = new Property(SkinProperty.SKIN_KEY, encodedValue, signature);
        return new Property[]{prop};
    }
}
