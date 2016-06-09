package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.bungee.listener.JoinListener;
import com.github.games647.changeskin.bungee.listener.PreLoginListener;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.SkinStorage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;

public class ChangeSkinBungee extends Plugin {

    private ChangeSkinCore core;
    private Configuration configuration;

    private Cache<UUID, Object> cooldowns;

    @Override
    public void onEnable() {
        //load config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException ioExc) {
                getLogger().log(Level.SEVERE, "Error saving default config", ioExc);
            }
        }

        core = new ChangeSkinCore(getLogger(), getDataFolder());
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            cooldowns = CacheBuilder.newBuilder()
                    .expireAfterWrite(configuration.getInt("cooldown"), TimeUnit.SECONDS)
                    .<UUID, Object>build();

            String driver = configuration.getString("storage.driver");
            String host = configuration.getString("storage.host", "");
            int port = configuration.getInt("storage.port", 3306);
            String database = configuration.getString("storage.database");

            String username = configuration.getString("storage.username", "");
            String password = configuration.getString("storage.password", "");
            SkinStorage storage = new SkinStorage(core, driver, host, port, database, username, password);
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

        getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));
        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().getPluginManager().registerCommand(this, new SetSkinCommand(this));
    }

    public String getName() {
        return getDescription().getName();
    }

    public void applySkin(ProxiedPlayer player, SkinData skinData) {
        Property textures = convertToProperty(skinData);

        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();
        //this is null on offline mode
        if (loginProfile == null) {
            try {
                Field profileField = initialHandler.getClass().getDeclaredField("loginProfile");
                profileField.setAccessible(true);
                LoginResult loginResult = new LoginResult(player.getUniqueId().toString(), new Property[]{textures});
                profileField.set(initialHandler, loginResult);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        } else {
            loginProfile.setProperties(new Property[]{textures});
        }

        //send plugin channel update request
        if (player.getServer() != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UpdateSkin");
            player.getServer().sendData(this.getDescription().getName(), out.toByteArray());
        }
    }

    public Property convertToProperty(SkinData skinData) {
        return new Property(ChangeSkinCore.SKIN_KEY, skinData.getEncodedData(), skinData.getEncodedSignature());
    }

    public void addCooldown(UUID invoker) {
        cooldowns.put(invoker, new Object());
    }

    public boolean isCooldown(UUID invoker) {
        return cooldowns.asMap().containsKey(invoker);
    }

    public Configuration getConfiguration() {
        return configuration;
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
        }

        //disallow - not whitelisted or blacklisted
        invoker.sendMessage(new ComponentBuilder("You don't have the permission to set this skin")
                .color(ChatColor.DARK_RED).create());
        return false;
    }
}
