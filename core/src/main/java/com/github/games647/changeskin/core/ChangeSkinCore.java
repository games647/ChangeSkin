package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.McApiProfile;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.PropertiesModel;
import com.github.games647.changeskin.core.model.TexturesModel;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
//    private static final String MCAPI_UUID_URL = "https://mcapi.de/api/user/";
//    private static final String MCAPI_UUID_URL = "https://us.mc-api.net/v3/uuid/";
    private static final String MCAPI_UUID_URL = "https://mcapi.ca/uuid/player/";
//    private static final String MCAPI_UUID_URL = "https://craftapi.com/api/user/uuid/";

    private static final int RATE_LIMIT_ID = 429;
    private static final String USER_AGENT = "ChangeSkin-Bukkit-Plugin";
    
    public static UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }

    private final Gson gson = new Gson();
    private final Map<String, String> localeMessages = Maps.newConcurrentMap();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<String, UUID> uuidCache = buildCache(3 * 60, 1024 * 5);;
    private final ConcurrentMap<String, Object> crackedNames = buildCache(3 * 60, 1024 * 5);
    private final ConcurrentMap<Object, Object> requests = buildCache(10, -1);

    private final Logger logger;
    private final File pluginFolder;
    private final int rateLimit;

    private SkinStorage storage;
    private long lastRateLimit;

    private final List<SkinData> defaultSkins = Lists.newArrayList();

    public ChangeSkinCore(Logger logger, File pluginFolder, int rateLimit) {
        this.logger = logger;
        this.pluginFolder = pluginFolder;
        this.rateLimit = rateLimit;
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

    public UUID getUUID(String playerName) throws NotPremiumException, RateLimitException {
        getLogger().log(Level.FINE, "Making UUID->Name request for {0}", playerName);
        if (!playerName.matches(VALID_USERNAME)) {
            return null;
        }

        if (requests.size() >= rateLimit || System.currentTimeMillis() - lastRateLimit < 1_000 * 60 * 10) {
//            logger.fine("STILL WAITING FOR RATE_LIMIT - TRYING SECOND API");
            return getUUIDFromAPI(playerName);
        }

        try {
            requests.put(new Object(), new Object());
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(UUID_URL + playerName).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("User-Agent", USER_AGENT);

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                throw new NotPremiumException(playerName);
            } else if (httpConnection.getResponseCode() == RATE_LIMIT_ID) {
                logger.info("RATE_LIMIT REACHED - TRYING THIRD-PARTY API");
                lastRateLimit = System.currentTimeMillis();
                return getUUIDFromAPI(playerName);
//                throw new RateLimitException(playerName);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                PlayerProfile playerProfile = gson.fromJson(line, PlayerProfile.class);
                String id = playerProfile.getId();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried converting player name to uuid", iOException);
        } catch (JsonParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }

    public UUID getUUIDFromAPI(String playerName) throws NotPremiumException, RateLimitException {
        if (!playerName.matches(VALID_USERNAME)) {
            return null;
        }

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(MCAPI_UUID_URL + playerName).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new NotPremiumException(playerName);
            } else if (httpConnection.getResponseCode() == RATE_LIMIT_ID) {
                throw new RateLimitException(playerName);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                McApiProfile playerProfile = gson.fromJson(line, McApiProfile.class);
                String id = playerProfile.getUuid();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried converting player name to uuid from third-party api", iOException);
        } catch (JsonParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from third-party api", parseException);
        }

        return null;
    }

    public SkinData downloadSkin(UUID ownerUUID) {
        //unsigned is needed in order to receive the signature
        String uuidString = ownerUUID.toString().replace("-", "") + "?unsigned=false";
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(SKIN_URL + uuidString).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                TexturesModel texturesModel = gson.fromJson(line, TexturesModel.class);

                PropertiesModel[] properties = texturesModel.getProperties();
                if (properties != null && properties.length > 0) {
                    PropertiesModel propertiesModel = properties[0];

                    //base64 encoded skin data
                    String encodedSkin = propertiesModel.getValue();
                    String signature = propertiesModel.getSignature();

                    SkinData skinData = new SkinData(encodedSkin, signature);
                    return skinData;
                }
            }
        } catch (IOException ioException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", ioException);
        } catch (JsonParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }

    public List<SkinData> getDefaultSkins() {
        return defaultSkins;
    }

    public void loadDefaultSkins(List<String> defaults) {
        for (String uuidString : defaults) {
            UUID ownerUUID = UUID.fromString(uuidString);
            SkinData skinData = storage.getSkin(ownerUUID);
            if (skinData == null) {
                skinData = downloadSkin(ownerUUID);
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
