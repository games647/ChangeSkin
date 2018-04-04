package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.net.HostAndPort;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import org.slf4j.Logger;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ChangeSkinCore {

    private final Map<String, String> localeMessages = new ConcurrentHashMap<>();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final Map<String, UUID> uuidCache = CommonUtil.buildCache(3 * 60 * 60, 1024 * 5);
    private final Map<String, Object> crackedNames = CommonUtil.buildCache(3 * 60 * 60, 1024 * 5);

    private final PlatformPlugin<?> plugin;
    private final List<SkinModel> defaultSkins = new ArrayList<>();
    private final List<Account> uploadAccounts = new ArrayList<>();
    private final MojangAuthApi authApi;

    private MojangSkinApi skinApi;
    private Configuration config;
    private SkinStorage storage;
    private CooldownService cooldownService;

    private Duration autoUpdateDiff;

    public ChangeSkinCore(PlatformPlugin<?> plugin) {
        this.plugin = plugin;
        this.authApi = new MojangAuthApi(plugin.getLog());
    }

    public void load(boolean database) {
        saveDefaultFile("messages.yml");
        saveDefaultFile("config.yml");

        try {
            config = loadFile("config.yml");
            int rateLimit = config.getInt("mojang-request-limit");

            cooldownService = new CooldownService(Duration.ofSeconds(config.getInt("cooldown")));

            autoUpdateDiff = Duration.ofMinutes(config.getInt("auto-skin-update"));
            List<HostAndPort> proxies = config.getStringList("proxies")
                    .stream().map(HostAndPort::fromString).collect(toList());
            skinApi = new MojangSkinApi(plugin.getLog(), rateLimit, proxies);

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
                            localeMessages.put(key, colored.replace("/newline", "\n"));
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

    public Map<String, UUID> getUuidCache() {
        return uuidCache;
    }

    public Map<String, Object> getCrackedNames() {
        return crackedNames;
    }

    public String getMessage(String key) {
        return localeMessages.get(key);
    }

    public List<SkinModel> getDefaultSkins() {
        return defaultSkins;
    }

    public SkinModel checkAutoUpdate(SkinModel oldSkin) {
        if (oldSkin == null) {
            return null;
        }

        if (oldSkin.isOutdated(autoUpdateDiff)) {
            Optional<SkinModel> updatedSkin = skinApi.downloadSkin(oldSkin.getProfileId());
            if (updatedSkin.isPresent() && !Objects.equals(updatedSkin.get(), oldSkin)) {
                return updatedSkin.get();
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
        try (Reader reader = Files.newBufferedReader(file)) {
            return configProvider.load(reader, defaults);
        }
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
        for (String id : defaults) {
            Integer rowId = Ints.tryParse(id);
            if (rowId != null) {
                Optional.ofNullable(storage.getSkin(rowId)).ifPresent(defaultSkins::add);
            }

            UUID ownerUUID = UUID.fromString(id);
            SkinModel skinData = storage.getSkin(ownerUUID);
            if (skinData == null) {
                Optional<SkinModel> optSkin = skinApi.downloadSkin(ownerUUID);
                if (optSkin.isPresent()) {
                    skinData = optSkin.get();
                    uuidCache.put(skinData.getProfileName(), skinData.getProfileId());
                    storage.save(skinData);
                }
            }

            defaultSkins.add(skinData);
        }
    }

    private void loadAccounts(Iterable<String> accounts) {
        for (String line : accounts) {
            String email = line.split(":")[0];
            String password = line.split(":")[1];

            authApi.authenticate(email, password).ifPresent(account -> {
                plugin.getLog().info("Authenticated user {}", account.getProfile().getId());
                uploadAccounts.add(account);
            });
        }
    }

    public void close() {
        defaultSkins.clear();
        uuidCache.clear();

        if (storage != null) {
            storage.close();
        }
    }

    public MojangSkinApi getSkinApi() {
        return skinApi;
    }

    public MojangAuthApi getAuthApi() {
        return authApi;
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
