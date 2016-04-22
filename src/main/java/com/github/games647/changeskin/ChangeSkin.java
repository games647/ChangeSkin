package com.github.games647.changeskin;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.github.games647.changeskin.listener.AsyncPlayerLoginListener;
import com.github.games647.changeskin.listener.PlayerLoginListener;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class ChangeSkin extends JavaPlugin {

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private final List<SkinData> defaultSkins = Lists.newArrayList();

    private Storage storage;

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<String, UUID> uuidCache = SafeCacheBuilder
            .<String, UUID>newBuilder()
            .maximumSize(1024 * 5)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build(new CacheLoader<String, UUID>() {

                @Override
                public UUID load(String playerName) throws Exception {
                    //A key should be inserted manually on start packet
                    throw new UnsupportedOperationException("Not supported");
                }
            });

    @Override
    public void onEnable() {
        saveDefaultConfig();

        storage = new Storage(this);
        try {
            storage.createTables();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to setup database. Disabling plugin...", ex);
            setEnabled(false);
            return;
        }

        loadDefaultSkins();

        getCommand("setskin").setExecutor(new SetSkinCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerLoginListener(this), this);
    }

    @Override
    public void onDisable() {
        //clean up
        if (storage != null) {
            storage.close();
        }

        defaultSkins.clear();
        uuidCache.clear();
    }

    public Storage getStorage() {
        return storage;
    }

    public List<SkinData> getDefaultSkins() {
        return defaultSkins;
    }

    public ConcurrentMap<String, UUID> getUuidCache() {
        return uuidCache;
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
                JSONObject userData = (JSONObject) JSONValue.parseWithException(line);

                JSONArray properties = (JSONArray) userData.get("properties");
                JSONObject data = (JSONObject) properties.get(0);

                //base64 encoded skin data
                String encodedSkin = (String) data.get("value");
                String signature = (String) data.get("signature");

                SkinData skinData = new SkinData(encodedSkin, signature);
                return skinData;
            }
        } catch (IOException ioException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", ioException);
        } catch (ParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
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
                JSONObject profile = (JSONObject) JSONValue.parseWithException(line);
                String id = (String) profile.get("id");
                return parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", iOException);
        } catch (ParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }

    //you should call this method async
    public void setSkin(Player player, final SkinData newSkin, boolean applyNow) {
        final UserPreferences preferences = storage.getPreferences(player.getUniqueId(), false);
        preferences.setTargetSkin(newSkin);
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                storage.save(newSkin);
                storage.save(preferences);
            }
        });
    }

    //you should call this method async
    public void setSkin(Player player, UUID targetSkin, boolean applyNow) {
        SkinData newSkin = storage.getSkin(targetSkin, true);
        if (newSkin == null) {
            newSkin = downloadSkin(targetSkin);
        }

        setSkin(player, newSkin, applyNow);
    }

    public static UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }

    private void loadDefaultSkins() {
        List<String> defaultList = getConfig().getStringList("default-skins");
        for (String uuidString : defaultList) {
            UUID ownerUUID = UUID.fromString(uuidString);

            SkinData skinData = downloadSkin(ownerUUID);
            defaultSkins.add(skinData);
        }
    }
}
