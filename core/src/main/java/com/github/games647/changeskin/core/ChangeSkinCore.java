package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeSkinCore {

    public static final String SKIN_KEY = "textures";

    private final Map<String, String> localeMessages = Maps.newConcurrentMap();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final Map<String, UUID> uuidCache = CommonUtil.buildCache(3 * 60 * 60, 1024 * 5);

    private final Map<String, Object> crackedNames = CommonUtil.buildCache(3 * 60 * 60, 1024 * 5);

    private final Logger logger;
    private final Path pluginFolder;

    private SkinStorage storage;

    private final List<SkinData> defaultSkins = Lists.newArrayList();
    private final MojangSkinApi mojangSkinApi;
    private final MojangAuthApi mojangAuthApi;
    private final Map<UUID, Object> cooldowns;
    private final int autoUpdateDiff;

    private final List<Account> uploadAccounts = Lists.newArrayList();

    public ChangeSkinCore(Logger logger, Path pluginFolder, int rateLimit, int cooldown, int autoUpdateDiff
            , List<HostAndPort> proxies) {
        this.logger = logger;
        this.pluginFolder = pluginFolder;
        this.mojangSkinApi = new MojangSkinApi(logger, rateLimit, proxies);
        this.mojangAuthApi = new MojangAuthApi(logger);

        if (cooldown <= 0) {
            cooldown = 1;
        }

        this.cooldowns = CommonUtil.buildCache(cooldown, -1);
        this.autoUpdateDiff = autoUpdateDiff * 60 * 1_000;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataFolder() {
        return pluginFolder;
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

    public void addMessage(String key, String message) {
        localeMessages.put(key, message);
    }

    public List<SkinData> getDefaultSkins() {
        return defaultSkins;
    }

    public int getAutoUpdateDiff() {
        return autoUpdateDiff;
    }

    public void loadDefaultSkins(Iterable<String> defaults) {
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

    public void loadAccounts(Iterable<String> accounts) {
        for (String line : accounts) {
            String email = line.split(":")[0];
            String password = line.split(":")[1];

            Account account = mojangAuthApi.authenticate(email, password);
            if (account != null) {
                logger.log(Level.INFO, "Successful authenticated user {0}", account.getProfile().getId());
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
