package com.github.games647.changeskin;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
        loadPreferences();

        getCommand("setskin").setExecutor(new SetSkinCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
    }

    @Override
    public void onDisable() {
        savePreferences();

        //clean up
        userPreferences.clear();
        skinCache.clear();
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
                return parseId(id);
            }
        } catch (IOException iOException) {
            getLogger().log(Level.SEVERE, "Tried downloading skin data from Mojang", iOException);
        } catch (ParseException parseException) {
            getLogger().log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
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

    private void savePreferences() {
        getDataFolder().mkdir();
        File file = new File(getDataFolder(), "preferences.txt");

        BufferedWriter bufferedWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            bufferedWriter = Files.newWriter(file, Charsets.UTF_8);
            for (Map.Entry<UUID, UUID> entry : userPreferences.entrySet()) {
                bufferedWriter.write(entry.getKey().toString());
                bufferedWriter.write(':');
                bufferedWriter.write(entry.getValue().toString());
                bufferedWriter.write(System.lineSeparator());
            }

            bufferedWriter.flush();
        } catch (IOException ioExc) {
            getLogger().log(Level.SEVERE, "Failed to save skin prefernces", ioExc);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ioExc) {
                    getLogger().log(Level.SEVERE, "Failed to close the file handle", ioExc);
                }
            }
        }
    }

    private void loadPreferences() {
        getDataFolder().mkdir();
        File file = new File(getDataFolder(), "preferences.txt");

        BufferedReader bufferedReader = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            bufferedReader = Files.newReader(file, Charsets.UTF_8);
            String currentLine = bufferedReader.readLine();
            while (currentLine != null && !currentLine.isEmpty()) {
                String[] parts = currentLine.split(":");
                UUID player = UUID.fromString(parts[0]);
                UUID target = UUID.fromString(parts[1]);
                userPreferences.put(player, target);

                currentLine = bufferedReader.readLine();
            }
        } catch (IOException ioExc) {
            getLogger().log(Level.SEVERE, "Failed to load preferences", ioExc);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioExc) {
                    getLogger().log(Level.SEVERE, "Failed to close the file handle", ioExc);
                }
            }
        }
    }
}
