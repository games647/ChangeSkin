package com.github.games647.changeskin.bukkit;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.bukkit.commands.SetSkinCommand;
import com.github.games647.changeskin.bukkit.commands.SkinInvalidateCommand;
import com.github.games647.changeskin.bukkit.listener.AsyncPlayerLoginListener;
import com.github.games647.changeskin.bukkit.listener.BungeeCordListener;
import com.github.games647.changeskin.bukkit.listener.PlayerLoginListener;
import com.github.games647.changeskin.bukkit.tasks.SkinUpdater;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.SkinStorage;
import com.github.games647.changeskin.core.UserPreference;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheLoader;

import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChangeSkinBukkit extends JavaPlugin {

    private boolean bungeeCord;

    protected ChangeSkinCore core;

    private ConcurrentMap<UUID, Object> cooldowns;
    private final ConcurrentMap<UUID, UserPreference> loginSessions = buildCache(2 * 60, -1);

    @Override
    public void onEnable() {
        try {
            bungeeCord = Bukkit.spigot().getConfig().getBoolean("settings.bungeecord");
        } catch (Exception | NoSuchMethodError ex) {
            getLogger().warning("Cannot check bungeecord support. You use a non-spigot build");
        }

        getCommand("setskin").setExecutor(new SetSkinCommand(this));
        getCommand("skinupdate").setExecutor(new SkinInvalidateCommand(this));

        if (bungeeCord) {
            getLogger().info("BungeeCord detected. Activating BungeeCord support");
            getLogger().info("Make sure you installed the plugin on BungeeCord too");

            getServer().getMessenger().registerOutgoingPluginChannel(this, getName());
            getServer().getMessenger().registerIncomingPluginChannel(this, getName(), new BungeeCordListener(this));
        } else {
            saveDefaultConfig();

            int cooldown = getConfig().getInt("cooldown");
            if (cooldown <= 1) {
                cooldown = 1;
            }

            cooldowns = buildCache(cooldown, -1);

            String driver = getConfig().getString("storage.driver");
            String host = getConfig().getString("storage.host", "");
            int port = getConfig().getInt("storage.port", 3306);
            String database = getConfig().getString("storage.database");

            String username = getConfig().getString("storage.username", "");
            String password = getConfig().getString("storage.password", "");

            int rateLimit = getConfig().getInt("mojang-request-limit");
            this.core = new ChangeSkinCore(getLogger(), getDataFolder(), rateLimit);
            SkinStorage storage = new SkinStorage(core, driver, host, port, database, username, password);
            core.setStorage(storage);
            try {
                storage.createTables();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to setup database. Disabling plugin...", ex);
                setEnabled(false);
                return;
            }

            core.loadDefaultSkins(getConfig().getStringList("default-skins"));

            loadLocale();

            getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
            getServer().getPluginManager().registerEvents(new AsyncPlayerLoginListener(this), this);
        }
    }

    public WrappedSignedProperty convertToProperty(SkinData skinData) {
        return WrappedSignedProperty.fromValues(ChangeSkinCore.SKIN_KEY, skinData.getEncodedData()
                , skinData.getEncodedSignature());
    }

    public void addCooldown(UUID invoker) {
        cooldowns.put(invoker, new Object());
    }

    public boolean isCooldown(UUID invoker) {
        return cooldowns.containsKey(invoker);
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
    public void setSkin(Player player, final SkinData newSkin, boolean applyNow) {
        new SkinUpdater(this, null, player, newSkin).run();
    }

    //you should call this method async
    public void setSkin(Player player, UUID targetSkin, boolean applyNow) {
        SkinData newSkin = core.getStorage().getSkin(targetSkin);
        if (newSkin == null) {
            newSkin = core.getMojangSkinApi().downloadSkin(targetSkin);
            core.getUuidCache().put(newSkin.getName(), newSkin.getUuid());
        }

        setSkin(player, newSkin, applyNow);
    }

    public boolean checkPermission(CommandSender invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(getName().toLowerCase() + ".skin.whitelist." + uuid.toString())) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessage(invoker, "no-permission");
        }

        return false;
    }

    public void sendMessage(CommandSender sender, String key) {
        if (core == null) {
            return;
        }

        String message = core.getMessage(key);
        if (message != null && sender != null) {
            sender.sendMessage(message);
        }
    }

    private void loadLocale() {
        File messageFile = new File(getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            saveResource("messages.yml", false);
        }

        InputStreamReader defaultReader = new InputStreamReader(getResource("messages.yml"), Charsets.UTF_8);
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defaultReader);

        YamlConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        messageConfig.setDefaults(defaults);

        for (String key : messageConfig.getKeys(false)) {
            String message = ChatColor.translateAlternateColorCodes('&', messageConfig.getString(key));
            if (!message.isEmpty()) {
                core.addMessage(key, message);
            }
        }
    }

    public boolean isBungeeCord() {
        return bungeeCord;
    }

    private <K, V> ConcurrentMap<K, V> buildCache(int seconds, int maxSize) {
        SafeCacheBuilder<Object, Object> builder = SafeCacheBuilder.newBuilder();

        if (seconds > 0) {
            builder.expireAfterWrite(seconds, TimeUnit.SECONDS);
        }

        if (maxSize > 0) {
            builder.maximumSize(maxSize);
        }

        return builder
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                });
    }
}
