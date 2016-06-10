package com.github.games647.changeskin.core;

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
    private final ConcurrentMap<String, UUID> uuidCache = CacheBuilder
            .<String, UUID>newBuilder()
            .maximumSize(1024 * 5)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build(new CacheLoader<String, UUID>() {
                @Override
                public UUID load(String key) throws Exception {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }).asMap();

    private final ConcurrentMap<UUID, UserPreferences> loginSession = CacheBuilder
            .newBuilder()
            //prevent memory leaks, because we don't if the player disconected during a login
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, UserPreferences>() {
                @Override
                public UserPreferences load(UUID key) throws Exception {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }).asMap();

    private final Logger logger;
    private final File pluginFolder;

    private SkinStorage storage;

    private final List<SkinData> defaultSkins = Lists.newArrayList();

    public ChangeSkinCore(Logger logger, File pluginFolder) {
        this.logger = logger;
        this.pluginFolder = pluginFolder;
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

    public String getMessage(String key) {
        return localeMessages.get(key);
    }

    public void addMessage(String key, String message) {
        localeMessages.put(key, message);
    }

    public UserPreferences getLoginSession(UUID id) {
        return loginSession.get(id);
    }

    public void startSession(UUID id, UserPreferences preferences) {
        loginSession.put(id, preferences);
    }

    public void endSession(UUID id) {
        loginSession.remove(id);
    }

    public UUID getUUID(String playerName) {
        if (!playerName.matches(VALID_USERNAME)) {
            return null;
        }

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(UUID_URL + playerName).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                PlayerProfile playerProfile = gson.fromJson(line, PlayerProfile.class);
                String id = playerProfile.getId();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", iOException);
        } catch (JsonParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
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
}
