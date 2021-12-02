package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.bukkit.bungee.CheckPermissionListener;
import com.github.games647.changeskin.bukkit.bungee.SkinUpdateListener;
import com.github.games647.changeskin.bukkit.command.InfoCommand;
import com.github.games647.changeskin.bukkit.command.InvalidateCommand;
import com.github.games647.changeskin.bukkit.command.SelectCommand;
import com.github.games647.changeskin.bukkit.command.SetCommand;
import com.github.games647.changeskin.bukkit.command.SkullCommand;
import com.github.games647.changeskin.bukkit.command.UploadCommand;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.message.ChannelMessage;
import com.github.games647.changeskin.core.message.NamespaceKey;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.slf4j.Logger;

import static com.github.games647.changeskin.core.message.CheckPermMessage.CHECK_PERM_CHANNEL;
import static com.github.games647.changeskin.core.message.ForwardMessage.FORWARD_COMMAND_CHANNEL;
import static com.github.games647.changeskin.core.message.PermResultMessage.PERMISSION_RESULT_CHANNEL;
import static com.github.games647.changeskin.core.message.SkinUpdateMessage.UPDATE_SKIN_CHANNEL;

public class ChangeSkinBukkit extends JavaPlugin implements PlatformPlugin<CommandSender> {

    private final ConcurrentMap<UUID, UserPreference> loginSessions = CommonUtil.buildCache(2 * 60, -1);
    private final Logger logger = CommonUtil.initializeLoggerService(getLogger());
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

            //outgoing
            Messenger messenger = getServer().getMessenger();
            String permissionResultChannel = new NamespaceKey(getName(), PERMISSION_RESULT_CHANNEL).getCombinedName();
            String forwardChannel = new NamespaceKey(getName(), FORWARD_COMMAND_CHANNEL).getCombinedName();
            messenger.registerOutgoingPluginChannel(this, permissionResultChannel);
            messenger.registerOutgoingPluginChannel(this, forwardChannel);

            //incoming
            String updateChannel = new NamespaceKey(getName(), UPDATE_SKIN_CHANNEL).getCombinedName();
            String permissionChannel = new NamespaceKey(getName(), CHECK_PERM_CHANNEL).getCombinedName();
            messenger.registerIncomingPluginChannel(this, updateChannel, new SkinUpdateListener(this));
            messenger.registerIncomingPluginChannel(this, permissionChannel, new CheckPermissionListener(this));
        } else {
            getServer().getPluginManager().registerEvents(new LoginListener(this), this);
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
        Optional.ofNullable(getCommand("setskin")).ifPresent(c -> c.setExecutor(new SetCommand(this)));
        Optional.ofNullable(getCommand("skinupdate")).ifPresent(c -> c.setExecutor(new InvalidateCommand(this)));
        Optional.ofNullable(getCommand("skinselect")).ifPresent(c -> c.setExecutor(new SelectCommand(this)));
        Optional.ofNullable(getCommand("skinupload")).ifPresent(c -> c.setExecutor(new UploadCommand(this)));
        Optional.ofNullable(getCommand("skinskull")).ifPresent(c -> c.setExecutor(new SkullCommand(this)));
        Optional.ofNullable(getCommand("skin-info")).ifPresent(c -> c.setExecutor(new InfoCommand(this)));
    }

    public boolean isBungeeCord() {
        return bungeeCord;
    }

    public void sendPluginMessage(PluginMessageRecipient sender, ChannelMessage message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        message.writeTo(out);

        NamespaceKey channel = new NamespaceKey(getName(), message.getChannelName());
        sender.sendPluginMessage(this, channel.getCombinedName(), out.toByteArray());
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
