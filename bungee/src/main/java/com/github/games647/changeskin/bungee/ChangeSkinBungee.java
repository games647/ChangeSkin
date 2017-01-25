package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.bungee.commands.SetSkinCommand;
import com.github.games647.changeskin.bungee.commands.SkinInvalidateCommand;
import com.github.games647.changeskin.bungee.commands.SkinSelectCommand;
import com.github.games647.changeskin.bungee.commands.SkinUploadCommand;
import com.github.games647.changeskin.bungee.listener.DisconnectListener;
import com.github.games647.changeskin.bungee.listener.JoinListener;
import com.github.games647.changeskin.bungee.listener.LoginListener;
import com.github.games647.changeskin.bungee.listener.PluginMessageListener;
import com.github.games647.changeskin.bungee.listener.ServerSwitchListener;
import com.github.games647.changeskin.bungee.tasks.SkinUpdater;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

public class ChangeSkinBungee extends Plugin {

    private ChangeSkinCore core;
    private Configuration configuration;

    private final ConcurrentMap<PendingConnection, UserPreference> loginSessions = Maps.newConcurrentMap();

    @Override
    public void onEnable() {
        //load config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = saveDefaultResource("config.yml");

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            int rateLimit = configuration.getInt("mojang-request-limit");
            boolean mojangDownload = configuration.getBoolean("independent-skin-downloading");
            int cooldown = configuration.getInt("cooldown");
            int updateDiff = configuration.getInt("auto-skin-update");
            core = new ChangeSkinCore(getLogger(), getDataFolder().toPath(), rateLimit, mojangDownload, cooldown, updateDiff);

            loadLocale();

            String driver = configuration.getString("storage.driver");
            String host = configuration.getString("storage.host", "");
            int port = configuration.getInt("storage.port", 3306);
            String database = configuration.getString("storage.database");

            String username = configuration.getString("storage.username", "");
            String password = configuration.getString("storage.password", "");

            String pluginName = this.getDescription().getName();
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat(pluginName + " Database Pool Thread #%1$d")
                    //Hikari create daemons by default
                    .setDaemon(true)
                    .setThreadFactory(new GroupedThreadFactory(this, pluginName)).build();
            SkinStorage storage = new SkinStorage(core, threadFactory, driver, host, port, database, username, password);
            core.setStorage(storage);
            try {
                storage.createTables();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to setup database. Disabling plugin...", ex);
                return;
            }

            core.loadDefaultSkins(configuration.getStringList("default-skins"));
        } catch (IOException ioExc) {
            getLogger().log(Level.SEVERE, "Error loading config. Disabling plugin...", ioExc);
            return;
        }

        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
        getProxy().getPluginManager().registerListener(this, new DisconnectListener(this));

        //this is required to listen to messages from the server
        getProxy().registerChannel(getDescription().getName());
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));

        getProxy().getPluginManager().registerCommand(this, new SetSkinCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SkinInvalidateCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SkinUploadCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SkinSelectCommand(this));
    }

    private File saveDefaultResource(String file) {
        File configFile = new File(getDataFolder(), file);
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream(file)) {
                Files.copy(in, configFile.toPath());
            } catch (IOException ioExc) {
                getLogger().log(Level.SEVERE, "Error saving default " + file, ioExc);
            }
        }

        return configFile;
    }

    public String getName() {
        return getDescription().getName();
    }

    //you should call this method async
    public void setSkin(ProxiedPlayer player, final SkinData newSkin, boolean applyNow) {
        new SkinUpdater(this, player, player, newSkin, false, false).run();
    }

    //you should call this method async
    public void setSkin(ProxiedPlayer player, UUID targetSkin, boolean applyNow) {
        SkinData newSkin = core.getStorage().getSkin(targetSkin);
        if (newSkin == null) {
            newSkin = core.getMojangSkinApi().downloadSkin(targetSkin);
        }

        setSkin(player, newSkin, applyNow);
    }

    public void applySkin(ProxiedPlayer player, SkinData skinData) {
        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();
        //this is null on offline mode
        if (loginProfile == null) {
            try {
                Field profileField = InitialHandler.class.getDeclaredField("loginProfile");
                profileField.setAccessible(true);
                String mojangUUID = player.getUniqueId().toString().replace("-", "");

                if (skinData == null) {
                    LoginResult loginResult = new LoginResult(mojangUUID, new Property[]{});
                    profileField.set(initialHandler, loginResult);
                } else {
                    Property textures = convertToProperty(skinData);
                    Property[] properties = new Property[]{textures};

                    LoginResult loginResult = new LoginResult(mojangUUID, properties);
                    profileField.set(initialHandler, loginResult);
                }
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        } else if (skinData == null) {
            loginProfile.setProperties(new Property[]{});
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
                out.writeUTF(skinData.getEncodedData());
                out.writeUTF(skinData.getEncodedSignature());
                out.writeUTF(player.getName());
            }

            player.getServer().sendData(getDescription().getName(), out.toByteArray());
        }
    }

    public Property convertToProperty(SkinData skinData) {
        return new Property(ChangeSkinCore.SKIN_KEY, skinData.getEncodedData(), skinData.getEncodedSignature());
    }

    public UUID getOfflineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    public Configuration getConfig() {
        return configuration;
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

    public boolean checkPermission(CommandSender invoker, UUID uuid) {
        if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist." + uuid.toString())) {
            return true;
        } else if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist.*")) {
            if (invoker.hasPermission('-' + getName().toLowerCase() + ".skin.whitelist." + uuid.toString())) {
                //backlisted explicit
                sendMessage(invoker, "no-permission");
                return false;
            }

            return true;
        }

        //disallow - not whitelisted or blacklisted
        sendMessage(invoker, "no-permission");
        return false;
    }

    public void sendMessage(CommandSender sender, String key) {
        String message = core.getMessage(key);
        if (message != null && sender != null) {
            sender.sendMessage(TextComponent.fromLegacyText(message));
        }
    }

    public void sendMessage(CommandSender sender, String key, Object... arguments) {
        String message = core.getMessage(key);
        if (message != null && sender != null) {
            sender.sendMessage(TextComponent.fromLegacyText(MessageFormat.format(message, arguments)));
        }
    }

    private void loadLocale() {
        try {
            File messageFile = saveDefaultResource("messages.yml");

            Configuration defaults = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(getClass().getResourceAsStream("/messages.yml"));

            Configuration messageConf = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(messageFile, defaults);
            for (String key : messageConf.getKeys()) {
                String message = ChatColor.translateAlternateColorCodes('&', messageConf.getString(key));
                if (!message.isEmpty()) {
                    core.addMessage(key, message);
                }
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error loading locale", ex);
        }
    }
}
