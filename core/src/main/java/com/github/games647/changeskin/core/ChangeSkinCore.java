package com.github.games647.changeskin.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChangeSkinCore {

    public static final String SKIN_KEY = "textures";
    
    public static UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }

    private final Map<String, String> localeMessages = Maps.newConcurrentMap();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<String, UUID> uuidCache = buildCache(3 * 60, 1024 * 5);;
    private final ConcurrentMap<String, Object> crackedNames = buildCache(3 * 60, 1024 * 5);

    private final Logger logger;
    private final File pluginFolder;

    private SkinStorage storage;

    private final List<SkinData> defaultSkins = Lists.newArrayList();
    private final MojangSkinApi mojangSkinApi;

    public ChangeSkinCore(Logger logger, File pluginFolder, int rateLimit) {
        this.logger = logger;
        this.pluginFolder = pluginFolder;
        this.mojangSkinApi = new MojangSkinApi(buildCache(10, -1), rateLimit, logger);
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

    public void onDisable() {
        defaultSkins.clear();
        uuidCache.clear();
    }

    public MojangSkinApi getMojangSkinApi() {
        return mojangSkinApi;
    }

    public void setStorage(SkinStorage storage) {
        this.storage = storage;
    }

    public SkinStorage getStorage() {
        return storage;
    }

    private <K, V> ConcurrentMap<K, V> buildCache(int minutes, int maxSize) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (minutes > 0) {
            builder.expireAfterWrite(minutes, TimeUnit.MINUTES);
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
}
