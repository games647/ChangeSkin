package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.StoredSkin;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.craftapi.RotatingProxySelector;
import com.github.games647.craftapi.model.auth.Account;
import com.github.games647.craftapi.model.skin.SkinModel;
import com.github.games647.craftapi.model.skin.SkinProperty;
import com.github.games647.craftapi.resolver.MojangResolver;
import com.github.games647.craftapi.resolver.RateLimitException;
import com.google.common.net.HostAndPort;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import org.slf4j.Logger;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ChangeSkinCore {

    private final Map<String, String> localeMessages = new ConcurrentHashMap<>();

    private final PlatformPlugin<?> plugin;
    private final List<StoredSkin> defaultSkins = new ArrayList<>();
    private final List<Account> uploadAccounts = new ArrayList<>();

    private final MojangResolver resolver = new MojangResolver();

    private Configuration config;
    private SkinStorage storage;
    private CooldownService cooldownService;

    private Duration autoUpdateDiff;

    public ChangeSkinCore(PlatformPlugin<?> plugin) {
        this.plugin = plugin;
    }

    public void load(boolean database) {
        saveDefaultFile("messages.yml");
        saveDefaultFile("config.yml");

        try {
            config = loadFile("config.yml");

            cooldownService = new CooldownService(Duration.ofMinutes(config.getInt("cooldown")));
            autoUpdateDiff = Duration.ofMinutes(config.getInt("auto-skin-update"));

            int rateLimit = config.getInt("mojang-request-limit");
            Set<Proxy> proxies = config.getStringList("proxies")
                    .stream()
                    .map(HostAndPort::fromString)
                    .map(proxy -> new InetSocketAddress(proxy.getHostText(), proxy.getPort()))
                    .map(sa -> new Proxy(Type.HTTP, sa))
                    .collect(toSet());

            resolver.setMaxNameRequests(rateLimit);
            resolver.setProxySelector(new RotatingProxySelector(proxies));

            if (database) {
                if (!setupDatabase(config.getSection("storage"))) {
                    return;
                }

                loadDefaultSkins(config.getStringList("default-skins"));
                loadAccounts(config.getStringList("upload-accounts"));
            }

            Configuration messages = loadFile("messages.yml");

            messages.getKeys()
                    .stream()
                    .filter(key -> messages.get(key) != null)
                    .collect(toMap(identity(), messages::get))
                    .forEach((key, message) -> {
                        String colored = CommonUtil.translateColorCodes((String) message);
                        if (!colored.isEmpty()) {
                            localeMessages.put(key, colored);
                        }
                    });
        } catch (IOException ioEx) {
            plugin.getLog().info("Failed to load yaml file", ioEx);
        }
    }

    public boolean setupDatabase(Configuration sqlConfig) {
        String driver = sqlConfig.getString("driver");
        if (!checkDriver(driver)) {
            return false;
        }

        String host = sqlConfig.get("host", "");
        int port = sqlConfig.get("port", 3306);
        String database = sqlConfig.getString("database");

        String user = sqlConfig.get("username", "");
        String password = sqlConfig.get("password", "");

        boolean useSSL = sqlConfig.get("useSSL", false);
        this.storage = new SkinStorage(this, driver, host, port, database, user, password, useSSL);
        try {
            this.storage.createTables();
            return true;
        } catch (Exception ex) {
            getLogger().error("Failed to setup database.", ex);
        }

        return false;
    }

    private boolean checkDriver(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException notFoundEx) {
            Logger log = plugin.getLog();
            log.error("Please choose for Spigot (SQLite, MySQL), Sponge (SQLite, MariaDB) or BungeeCord (MySQL)");
            log.error("This driver {} is not supported on this platform", className, notFoundEx);
        }

        return false;
    }

    public Logger getLogger() {
        return plugin.getLog();
    }

    public PlatformPlugin<?> getPlugin() {
        return plugin;
    }

    public String getMessage(String key) {
        return localeMessages.get(key);
    }

    public List<StoredSkin> getDefaultSkins() {
        return defaultSkins;
    }

    public SkinModel checkAutoUpdate(SkinModel oldSkin) {
        if (oldSkin == null) {
            return null;
        }

        Duration difference = Duration.between(oldSkin.getTimestamp(), Instant.now());
        if (difference.compareTo(autoUpdateDiff) >= 0) {
            try {
                Optional<SkinProperty> property = resolver.downloadSkin(oldSkin.getOwnerId());
                if (property.isPresent()) {
                    SkinModel updateSkin = resolver.decodeSkin(property.get());
                    if (!Objects.equals(updateSkin, oldSkin)) {
                        return updateSkin;
                    }
                }
            } catch (IOException | RateLimitException ex) {
                plugin.getLog().warn("Failed to update skin", ex);
            }
        }

        return oldSkin;
    }

    private Configuration loadFile(String fileName) throws IOException {
        Configuration defaults;

        ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        try (InputStream defaultStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            defaults = configProvider.load(defaultStream);
        }

        Path file = plugin.getPluginFolder().resolve(fileName);
        return configProvider.load(Files.newBufferedReader(file), defaults);
    }

    private void saveDefaultFile(String fileName) {
        Path dataFolder = plugin.getPluginFolder();
        try {
            if (Files.notExists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path configFile = dataFolder.resolve(fileName);
            if (Files.notExists(configFile)) {
                try (InputStream defaultStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
                    Files.copy(defaultStream, configFile);
                }
            }
        } catch (IOException ioExc) {
            plugin.getLog().error("Cannot create default file {} in {}", fileName, dataFolder, ioExc);
        }
    }

    private void loadDefaultSkins(Iterable<String> defaults) {
        for (String uuidString : defaults) {
            UUID ownerUUID = UUID.fromString(uuidString);
            StoredSkin skinData = storage.getSkin(ownerUUID);
            if (skinData == null) {
                try {
                    Optional<SkinProperty> skinProperty = resolver.downloadSkin(ownerUUID);
                    if (skinProperty.isPresent()) {
                        SkinProperty property = skinProperty.get();
                        SkinModel skinModel = resolver.decodeSkin(property);

                        storage.save(property);
                        skinData = property;
                    }
                } catch (IOException | RateLimitException ex) {
                    getLogger().error("Failed download default skin", ex);
                }
            }

            defaultSkins.add(skinData);
        }
    }

    private void loadAccounts(Iterable<String> accounts) {
        for (String line : accounts) {
            String email = line.split(":")[0];
            String password = line.split(":")[1];

            try {
                Account account = resolver.authenticate(email, password);

                plugin.getLog().info("Authenticated user {}", account.getProfile().getId());
                uploadAccounts.add(account);
            } catch (IOException ex) {
                plugin.getLog().error("Failed to authenticate user: {}", email, ex);
            }
        }
    }

    public void close() {
        defaultSkins.clear();

        if (storage != null) {
            storage.close();
        }
    }

    public MojangResolver getResolver() {
        return resolver;
    }

    public SkinStorage getStorage() {
        return storage;
    }

    public Configuration getConfig() {
        return config;
    }

    public CooldownService getCooldownService() {
        return cooldownService;
    }

    public List<Account> getUploadAccounts() {
        return uploadAccounts;
    }
}
