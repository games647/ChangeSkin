package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.bukkit.commands.InvalidateCommand;
import com.github.games647.changeskin.bukkit.commands.SelectCommand;
import com.github.games647.changeskin.bukkit.commands.SetCommand;
import com.github.games647.changeskin.bukkit.commands.SkullCommand;
import com.github.games647.changeskin.bukkit.commands.UploadCommand;
import com.github.games647.changeskin.bukkit.listener.AsyncLoginListener;
import com.github.games647.changeskin.bukkit.listener.BungeeListener;
import com.github.games647.changeskin.bukkit.listener.LoginListener;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.messages.ChannelMessage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.slf4j.Logger;

public class ChangeSkinBukkit extends JavaPlugin implements PlatformPlugin<CommandSender> {

    private final ConcurrentMap<UUID, UserPreference> loginSessions = CommonUtil.buildCache(2 * 60, -1);
    private final Logger logger = CommonUtil.createLoggerFromJDK(getLogger());
    private final ChangeSkinCore core = new ChangeSkinCore(this);

    private boolean bungeeCord;
    private final BukkitSkinAPI api = new BukkitSkinAPI(this);

    @Override
    public void onEnable() {
        try {
            bungeeCord = getServer().spigot().getConfig().getBoolean("settings.bungeecord");
        } catch (Exception | NoSuchMethodError ex) {
            logger.warn("Cannot check bungeecord support. You use a non-Spigot build");
        }

        registerCommands();

        try {
            core.load(!bungeeCord);
        } catch (Exception ex) {
            logger.error("Error initializing plugin. Disabling...", ex);
            setEnabled(false);
            return;
        }

        if (bungeeCord) {
            logger.info("BungeeCord detected. Activating BungeeCord support");
            logger.info("Make sure you installed the plugin on BungeeCord too");

            getServer().getMessenger().registerOutgoingPluginChannel(this, getName());
            getServer().getMessenger().registerIncomingPluginChannel(this, getName(), new BungeeListener(this));
        } else {
            getServer().getPluginManager().registerEvents(new LoginListener(this), this);
            getServer().getPluginManager().registerEvents(new AsyncLoginListener(this), this);
        }
    }

    @Override
    public void onDisable() {
        this.core.close();
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    public BukkitSkinAPI getApi() {
        return api;
    }

    public SkinStorage getStorage() {
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

    @Override
    public boolean hasSkinPermission(CommandSender invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist." + uuid)) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessage(invoker, "no-permission");
        }

        return false;
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

    public void sendPluginMessage(PluginMessageRecipient sender, ChannelMessage message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(message.getChannelName());

        message.writeTo(out);
        sender.sendPluginMessage(this, getName(), out.toByteArray());
    }

    @Override
    public void sendMessage(CommandSender receiver, String key) {
        String message = core.getMessage(key);
        if (message != null && receiver != null) {
            receiver.sendMessage(message);
        }
    }

    @Override
    public Logger getLog() {
        return logger;
    }

    @Override
    public Path getPluginFolder() {
        return getDataFolder().toPath();
    }
}
