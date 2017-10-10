package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.bungee.commands.InvalidateCommand;
import com.github.games647.changeskin.bungee.commands.SelectCommand;
import com.github.games647.changeskin.bungee.commands.SetCommand;
import com.github.games647.changeskin.bungee.commands.UploadCommand;
import com.github.games647.changeskin.bungee.listener.ConnectListener;
import com.github.games647.changeskin.bungee.listener.MessageListener;
import com.github.games647.changeskin.bungee.listener.ServerSwitchListener;
import com.github.games647.changeskin.bungee.tasks.SkinUpdater;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeSkinBungee extends Plugin implements PlatformPlugin<CommandSender> {

    //speed by letting the JVM optimize this
    //MethodHandle is only faster for static final fields
    private static final MethodHandle profileSetter;

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

    private final Map<PendingConnection, UserPreference> loginSessions = Maps.newConcurrentMap();
    private final Property[] emptyProperties = {};

    private ChangeSkinCore core;
    private Logger logger;

    @Override
    public void onEnable() {
        logger = CommonUtil.createLoggerFromJDK(getLogger());

        core = new ChangeSkinCore(this);
        try {
            core.load();
        } catch (Exception ioExc) {
            logger.error("Error loading config. Disabling plugin...", ioExc);
            return;
        }

        getProxy().getPluginManager().registerListener(this, new ConnectListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));

        //this is required to listen to messages from the server
        getProxy().registerChannel(getDescription().getName());
        getProxy().getPluginManager().registerListener(this, new MessageListener(this));

        getProxy().getPluginManager().registerCommand(this, new SetCommand(this));
        getProxy().getPluginManager().registerCommand(this, new InvalidateCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UploadCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SelectCommand(this));
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.close();
        }
    }

    @Override
    public String getName() {
        return getDescription().getName();
    }

    @Override
    public Logger getLog() {
        return logger;
    }

    @Override
    public Path getPluginFolder() {
        return getDataFolder().toPath();
    }

    @Override
    public void sendMessage(CommandSender receiver, String key) {
        String message = core.getMessage(key);
        if (message != null && receiver != null) {
            receiver.sendMessage(TextComponent.fromLegacyText(message));
        }
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return new ThreadFactoryBuilder()
                .setNameFormat(getName() + " Database Pool Thread #%1$d")
                //Hikari create daemons by default
                .setDaemon(true)
                .setThreadFactory(new GroupedThreadFactory(this, getName()))
                .build();
    }

    //you should call this method async
    public void setSkin(ProxiedPlayer player, final SkinModel newSkin, boolean applyNow) {
        new SkinUpdater(this, player, player, newSkin, false, false).run();
    }

    //you should call this method async
    public void setSkin(ProxiedPlayer player, UUID targetSkin, boolean applyNow) {
        SkinModel newSkin = core.getStorage().getSkin(targetSkin);
        if (newSkin == null) {
            Optional<SkinModel> downloadSkin = core.getSkinApi().downloadSkin(targetSkin);
            if (downloadSkin.isPresent()) {
                newSkin = downloadSkin.get();
            }
        }

        setSkin(player, newSkin, applyNow);
    }

    public void applySkin(ProxiedPlayer player, SkinModel skinData) {
        logger.debug("Applying skin for {}", player.getName());

        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();
        //this is null on offline mode
        if (loginProfile == null) {
            String mojangUUID = CommonUtil.toMojangId(player.getUniqueId());

            Property[] properties = emptyProperties;
            if (skinData != null) {
                Property textures = convertToProperty(skinData);
                properties = new Property[]{textures};
            }

            if (profileSetter != null) {
                try {
                    LoginResult loginResult = new LoginResult(mojangUUID, player.getName(), properties);
                    profileSetter.invokeExact(initialHandler, loginResult);
                } catch (Error error) {
                    //rethrow errors we shouldn't silence them like OutOfMemory
                    throw error;
                } catch (Throwable throwable) {
                    logger.error("Error applying skin", throwable);
                }
            }
        } else if (skinData == null) {
            loginProfile.setProperties(emptyProperties);
        } else {
            Property textures = convertToProperty(skinData);
            loginProfile.setProperties(new Property[]{textures});
        }

        //send plugin channel update request
        if (player.getServer() != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UpdateSkin");

            if (skinData == null) {
                out.writeUTF("null");
                out.writeUTF(player.getName());
            } else {
                out.writeUTF(skinData.getEncodedValue());
                out.writeUTF(skinData.getSignature());
                out.writeUTF(player.getName());
            }

            player.getServer().sendData(getDescription().getName(), out.toByteArray());
        }
    }

    public Property convertToProperty(SkinModel skinData) {
        return new Property(ChangeSkinCore.SKIN_KEY, skinData.getEncodedValue(), skinData.getSignature());
    }

    public UserPreference getLoginSession(PendingConnection id) {
        return loginSessions.get(id);
    }

    public void startSession(PendingConnection id, UserPreference preferences) {
        loginSessions.put(id, preferences);
    }

    public void endSession(PendingConnection id) {
        loginSessions.remove(id);
    }

    public SkinStorage getStorage() {
        return core.getStorage();
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    @Override
    public boolean checkWhitelistPermission(CommandSender invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist." + uuid)) {
            return true;
        } else if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist.*")) {
            if (invoker.hasPermission('-' + getName().toLowerCase() + ".skin.whitelist." + uuid)) {
                //blacklisted explicit
                if (sendMessage) {
                    sendMessage(invoker, "no-permission");
                }

                return false;
            }

            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessage(invoker, "no-permission");
        }

        return false;
    }
}
