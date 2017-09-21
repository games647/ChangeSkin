package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.bungee.commands.SetSkinCommand;
import com.github.games647.changeskin.bungee.commands.SkinInvalidateCommand;
import com.github.games647.changeskin.bungee.commands.SkinSelectCommand;
import com.github.games647.changeskin.bungee.commands.SkinUploadCommand;
import com.github.games647.changeskin.bungee.listener.ConnectListener;
import com.github.games647.changeskin.bungee.listener.PluginMessageListener;
import com.github.games647.changeskin.bungee.listener.ServerSwitchListener;
import com.github.games647.changeskin.bungee.tasks.SkinUpdater;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
            int cooldown = configuration.getInt("cooldown");
            int updateDiff = configuration.getInt("auto-skin-update");
            List<String> proxyList = (List<String>) configuration.getList("proxies", Lists.newArrayList());
            Map<String, Integer> proxies = proxyList.stream()
                    .collect(Collectors
                            .toMap(line -> line.split(":")[0], line -> Integer.parseInt(line.split(":")[1])));
            core = new ChangeSkinCore(getLogger(), getDataFolder().toPath(), rateLimit, cooldown, updateDiff, proxies);

            loadLocale();

            String driver = configuration.getString("storage.driver");
            String host = configuration.getString("storage.host", "");
            int port = configuration.getInt("storage.port", 3306);
            String database = configuration.getString("storage.database");

            String username = configuration.getString("storage.username", "");
            String password = configuration.getString("storage.password", "");

            boolean useSSL = configuration.getBoolean("storage.useSSL", false);

            String pluginName = this.getDescription().getName();
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat(pluginName + " Database Pool Thread #%1$d")
                    //Hikari create daemons by default
                    .setDaemon(true)
                    .setThreadFactory(new GroupedThreadFactory(this, pluginName)).build();
            SkinStorage storage = new SkinStorage(core, threadFactory, driver, host, port, database, username, password, useSSL);
            core.setStorage(storage);
            try {
                storage.createTables();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to setup database. Disabling plugin...", ex);
                return;
            }

            core.loadDefaultSkins(configuration.getStringList("default-skins"));
            core.loadAccounts(configuration.getStringList("upload-accounts"));
        } catch (IOException ioExc) {
            getLogger().log(Level.SEVERE, "Error loading config. Disabling plugin...", ioExc);
            return;
        }

        getProxy().getPluginManager().registerListener(this, new ConnectListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));

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
        getLogger().log(Level.FINE, "Applying skin for {0}", player.getName());

        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();
        //this is null on offline mode
        if (loginProfile == null) {
            try {
                Field profileField = InitialHandler.class.getDeclaredField("loginProfile");
                profileField.setAccessible(true);
                String mojangUUID = player.getUniqueId().toString().replace("-", "");

                Property[] properties = {};
                if (skinData != null) {
                    Property textures = convertToProperty(skinData);
                    properties = new Property[]{textures};
                }

                LoginResult loginResult = createResult(mojangUUID, player.getName(), properties);
                profileField.set(initialHandler, loginResult);
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

    private LoginResult createResult(String id, String name, Property[] properties) {
        Constructor<LoginResult> cons;
        try {
            cons = LoginResult.class.getConstructor(String.class, String.class, Property[].class);
            return cons.newInstance(id, name, properties);
        } catch (NoSuchMethodException e) {
            //old BungeeCord
            try {
                cons = LoginResult.class.getConstructor(String.class, Property[].class);
                return cons.newInstance(id, properties);
            } catch (Exception ex) {
                getLogger().log(Level.INFO, "Cannot invoke constructor for creating a fake skin", ex);
            }
        } catch (Exception ex) {
            getLogger().log(Level.INFO, "Cannot invoke constructor for creating a fake skin", ex);
        }

        return null;
    }

    public Property convertToProperty(SkinData skinData) {
        return new Property(ChangeSkinCore.SKIN_KEY, skinData.getEncodedData(), skinData.getEncodedSignature());
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
                //blacklisted explicit
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
