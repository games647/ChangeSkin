package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeSkinCore {

    public static final String SKIN_KEY = "textures";

    private static final int TIMEOUT = 3000;
    private static final String USER_AGENT = "ChangeSkin-Bukkit-Plugin";

    public static UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }

    public static <K, V> ConcurrentMap<K, V> buildCache(int seconds, int maxSize) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (seconds > 0) {
            builder.expireAfterWrite(seconds, TimeUnit.SECONDS);
        }

        if (maxSize > 0) {
            builder.maximumSize(maxSize);
        }

        return builder.build(new CacheLoader<K, V>() {
            @Override
            public V load(K key) throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }).asMap();
    }
    
    public static HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        httpConnection.setConnectTimeout(TIMEOUT);
        httpConnection.setReadTimeout(2 * TIMEOUT);
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        return httpConnection;
    }

    private final Map<String, String> localeMessages = Maps.newConcurrentMap();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<String, UUID> uuidCache = buildCache(3 * 60 * 60, 1024 * 5);

    private final ConcurrentMap<String, Object> crackedNames = buildCache(3 * 60 * 60, 1024 * 5);

    private final Logger logger;
    private final File pluginFolder;

    private SkinStorage storage;

    private final List<SkinData> defaultSkins = Lists.newArrayList();
    private final MojangSkinApi mojangSkinApi;
    private final MojangAuthApi mojangAuthApi;
    private ConcurrentMap<UUID, Object> cooldowns;
    private final int autoUpdateDiff;

    private final List<Account> uploadAccounts = Lists.newArrayList();

    public ChangeSkinCore(Logger logger, File pluginFolder, int rateLimit, boolean mojangDownload
            , int cooldown, int autoUpdateDiff) {
        this.logger = logger;
        this.pluginFolder = pluginFolder;
        this.mojangSkinApi = new MojangSkinApi(buildCache(10, -1), logger, rateLimit, mojangDownload);
        this.mojangAuthApi = new MojangAuthApi(logger);

        if (cooldown <= 0) {
            cooldown = 1;
        }

        this.cooldowns = buildCache(cooldown, -1);
        this.autoUpdateDiff = autoUpdateDiff * 60 * 1_000;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return pluginFolder;
    }

    public ConcurrentMap<String, UUID> getUuidCache() {
        return uuidCache;
    }

    public ConcurrentMap<String, Object> getCrackedNames() {
        return crackedNames;
    }

    public String getMessage(String key) {
        return localeMessages.get(key);
    }

    public void addMessage(String key, String message) {
        localeMessages.put(key, message);
    }

    public List<SkinData> getDefaultSkins() {
        return defaultSkins;
    }

    public int getAutoUpdateDiff() {
        return autoUpdateDiff;
    }

    public void loadDefaultSkins(List<String> defaults) {
        for (String uuidString : defaults) {
            UUID ownerUUID = UUID.fromString(uuidString);
            SkinData skinData = storage.getSkin(ownerUUID);
            if (skinData == null) {
                skinData = mojangSkinApi.downloadSkin(ownerUUID);
                uuidCache.put(skinData.getName(), skinData.getUuid());
                storage.save(skinData);
            }

            defaultSkins.add(skinData);
        }
    }

    public void loadAccounts(List<String> accounts) {
        for (String line : accounts) {
            String email = line.split(":")[0];
            String password = line.split(":")[1];

            Account account = mojangAuthApi.authenticate(email, password);
            if (account != null) {
                logger.log(Level.INFO, "Successfull authenticated user {0}", account.getProfile().getId());
                uploadAccounts.add(account);
            }
        }
    }

    public void onDisable() {
        defaultSkins.clear();
        uuidCache.clear();
    }

    public MojangSkinApi getMojangSkinApi() {
        return mojangSkinApi;
    }

    public MojangAuthApi getMojangAuthApi() {
        return mojangAuthApi;
    }

    public void setStorage(SkinStorage storage) {
        this.storage = storage;
    }

    public SkinStorage getStorage() {
        return storage;
    }

    public void addCooldown(UUID invoker) {
        cooldowns.put(invoker, new Object());
    }

    public boolean isCooldown(UUID invoker) {
        return cooldowns.containsKey(invoker);
    }

    public List<Account> getUploadAccounts() {
        return uploadAccounts;
    }
}
