package com.github.games647.changeskin;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class ChangeSkin extends JavaPlugin {

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private final Map<UUID, UUID> userPreferences = Maps.newHashMap();

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<UUID, WrappedSignedProperty> skinCache = SafeCacheBuilder
            .<UUID, WrappedSignedProperty>newBuilder()
            .maximumSize(1024 * 5)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, WrappedSignedProperty>() {

                @Override
                public WrappedSignedProperty load(UUID key) throws Exception {
                    //A key should be inserted manually on start packet
                    throw new UnsupportedOperationException("Not supported");
                }
            });

    @Override
    public void onEnable() {
        getCommand("setskin").setExecutor(new SetSkinCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
    }

    public ConcurrentMap<UUID, WrappedSignedProperty> getSkinCache() {
        return skinCache;
    }

    public Map<UUID, UUID> getUserPreferences() {
        return userPreferences;
    }

    public WrappedSignedProperty downloadSkin(UUID uuid) {
        //unsigned is needed in order to receive the signature
        String uuidString = uuid.toString().replace("-", "") + "?unsigned=false";
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(SKIN_URL + uuidString).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                JSONObject userData = (JSONObject) JSONValue.parseWithException(line);

                JSONArray properties = (JSONArray) userData.get("properties");

                JSONObject skinData = (JSONObject) properties.get(0);
                //base64 encoded skin data
                String encodedSkin = (String) skinData.get("value");
                String signature = (String) skinData.get("signature");

                return WrappedSignedProperty.fromValues("textures", encodedSkin, signature);
            }
        } catch (IOException ioException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", ioException);
        } catch (ParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }

    public UUID getUUID(String playerName) throws ParseException {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(UUID_URL + playerName).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                JSONArray profiles = (JSONArray) JSONValue.parseWithException(line);
                JSONObject profile = (JSONObject) profiles.get(0);


                String id = (String) profile.get("id");
                String name = (String) profile.get("name");
                return parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", iOException);
        }

        return null;
    }

    public void setSkin(Player player, UUID targetSkin) {
        userPreferences.put(player.getUniqueId(), targetSkin);
        if (!skinCache.containsKey(targetSkin)) {
            //download the skin only if it's not already in the cache
            Bukkit.getScheduler().runTaskAsynchronously(this, new SkinDownloader(this, player, targetSkin));
        }
    }

    private UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }
}
