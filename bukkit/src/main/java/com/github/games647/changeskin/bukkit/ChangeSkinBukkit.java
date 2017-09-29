package com.github.games647.changeskin.bukkit;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.bukkit.commands.InvalidateCommand;
import com.github.games647.changeskin.bukkit.commands.SelectCommand;
import com.github.games647.changeskin.bukkit.commands.SetCommand;
import com.github.games647.changeskin.bukkit.commands.SkullCommand;
import com.github.games647.changeskin.bukkit.commands.UploadCommand;
import com.github.games647.changeskin.bukkit.listener.AsyncLoginListener;
import com.github.games647.changeskin.bukkit.listener.BungeeListener;
import com.github.games647.changeskin.bukkit.listener.LoginListener;
import com.github.games647.changeskin.bukkit.tasks.SkinUpdater;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ChangeSkinBukkit extends JavaPlugin implements PlatformPlugin<CommandSender> {

    private final ConcurrentMap<UUID, UserPreference> loginSessions = CommonUtil.buildCache(2 * 60, -1);
    private final Logger logger = CommonUtil.createLoggerFromJDK(getLogger());

    private boolean bungeeCord;
    private ChangeSkinCore core;

    @Override
    public void onEnable() {
        try {
            bungeeCord = getServer().spigot().getConfig().getBoolean("settings.bungeecord");
        } catch (Exception | NoSuchMethodError ex) {
            logger.warn("Cannot check bungeecord support. You use a non-spigot build");
        }

        saveDefaultConfig();
        registerCommands();

        if (bungeeCord) {
            logger.info("BungeeCord detected. Activating BungeeCord support");
            logger.info("Make sure you installed the plugin on BungeeCord too");

            getServer().getMessenger().registerOutgoingPluginChannel(this, getName());
            getServer().getMessenger().registerIncomingPluginChannel(this, getName(), new BungeeListener(this));
        } else {
            this.core = new ChangeSkinCore(this);

            try {
                core.load();
            } catch (Exception ex) {
                logger.error("Error loading config. Disabling plugin...", ex);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            getServer().getPluginManager().registerEvents(new LoginListener(this), this);
            getServer().getPluginManager().registerEvents(new AsyncLoginListener(this), this);
        }
    }

    public WrappedSignedProperty convertToProperty(SkinData skinData) {
        return WrappedSignedProperty.fromValues(ChangeSkinCore.SKIN_KEY, skinData.getEncodedData()
                , skinData.getEncodedSignature());
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    @Override
    public void onDisable() {
        if (core != null) {
            this.core.onDisable();
        }
    }

    public SkinStorage getStorage() {
        if (core == null) {
            return null;
        }

        return core.getStorage();
    }

    public UserPreference getLoginSession(UUID id) {
        return loginSessions.get(id);
    }

    public void startSession(UUID id, UserPreference preferences) {
        loginSessions.put(id, preferences);
    }

    public void endSession(UUID id) {
        loginSessions.remove(id);
    }

    //you should call this method async
    public void setSkin(Player player, SkinData newSkin, boolean applyNow) {
        new SkinUpdater(this, null, player, newSkin, true).run();
    }

    //you should call this method async
    public void setSkin(Player player, UUID targetSkin, boolean applyNow) {
        SkinData newSkin = core.getStorage().getSkin(targetSkin);
        if (newSkin == null) {
            Optional<SkinData> downloadSkin = core.getSkinApi().downloadSkin(targetSkin);
            if (downloadSkin.isPresent()) {
                newSkin = downloadSkin.get();
            }
        }

        setSkin(player, newSkin, applyNow);
    }

    public boolean checkPermission(CommandSender invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist." + uuid)) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessage(invoker, "no-permission");
        }

        return false;
    }

    public void sendMessage(CommandSender sender, String key, Object... arguments) {
        if (core == null) {
            return;
        }

        String message = core.getMessage(key);
        if (message != null && sender != null) {
            sender.sendMessage(MessageFormat.format(message, arguments));
        }
    }

    private void registerCommands() {
        getCommand("setskin").setExecutor(new SetCommand(this));
        getCommand("skinupdate").setExecutor(new InvalidateCommand(this));
        getCommand("skinselect").setExecutor(new SelectCommand(this));
        getCommand("skinupload").setExecutor(new UploadCommand(this));
        getCommand("skinskull").setExecutor(new SkullCommand(this));
    }

    public boolean isBungeeCord() {
        return bungeeCord;
    }

    @Override
    public void sendMessage(CommandSender receiver, String message) {
        receiver.sendMessage(message);
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return null;
    }

    @Override
    public Logger getLog() {
        return logger;
    }
}
