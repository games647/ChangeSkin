package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.ApiPropertiesModel;
import com.github.games647.changeskin.core.model.McApiProfile;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.RawPropertiesModel;
import com.github.games647.changeskin.core.model.mojang.PropertiesModel;
import com.github.games647.changeskin.core.model.mojang.TexturesModel;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MojangSkinApi {

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String MCAPI_SKIN_URL = "https://mcapi.de/api/user/";


    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
//    private static final String MCAPI_UUID_URL = "https://mcapi.de/api/user/";
//    private static final String MCAPI_UUID_URL = "https://us.mc-api.net/v3/uuid/";
    private static final String MCAPI_UUID_URL = "https://mcapi.ca/uuid/player/";
//    private static final String MCAPI_UUID_URL = "https://craftapi.com/api/user/uuid/";

    private static final int RATE_LIMIT_ID = 429;
    private static final String USER_AGENT = "ChangeSkin-Bukkit-Plugin";

    private final Gson gson = new Gson();

    private final ConcurrentMap<Object, Object> requests;
    private final Logger logger;
    private final int rateLimit;
    private final boolean mojangDownload;

    private long lastRateLimit;

    public MojangSkinApi(ConcurrentMap<Object, Object> requests, Logger logger, int rateLimit, boolean mojangDownload) {
        this.requests = requests;
        this.rateLimit = rateLimit;
        this.logger = logger;
        this.mojangDownload = mojangDownload;
    }

    public UUID getUUID(String playerName) throws NotPremiumException, RateLimitException {
        logger.log(Level.FINE, "Making UUID->Name request for {0}", playerName);

        if (requests.size() >= rateLimit || System.currentTimeMillis() - lastRateLimit < 1_000 * 60 * 10) {
//            logger.fine("STILL WAITING FOR RATE_LIMIT - TRYING SECOND API");
            return getUUIDFromAPI(playerName);
        }

        BufferedReader reader = null;
        InputStreamReader inputReader = null;
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
            }

            inputReader = new InputStreamReader(httpConnection.getInputStream());
            reader = new BufferedReader(inputReader);
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                PlayerProfile playerProfile = gson.fromJson(line, PlayerProfile.class);
                String id = playerProfile.getId();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException iOException) {
            logger.log(Level.SEVERE, "Tried converting player name to uuid", iOException);
        } catch (JsonParseException parseException) {
            logger.log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        } finally {
            Closeables.closeQuietly(inputReader);
            Closeables.closeQuietly(reader);
        }

        return null;
    }

    public UUID getUUIDFromAPI(String playerName) throws NotPremiumException, RateLimitException {
        InputStreamReader inputReader = null;
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(MCAPI_UUID_URL + playerName).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            inputReader = new InputStreamReader(httpConnection.getInputStream());
            String line = CharStreams.toString(inputReader);
            if (line != null && !line.equals("null")) {
                PlayerProfile playerProfile = gson.fromJson(line, PlayerProfile[].class)[0];
                String id = playerProfile.getId();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException iOException) {
            logger.log(Level.SEVERE, "Tried converting player name to uuid from third-party api", iOException);
        } catch (JsonParseException parseException) {
            logger.log(Level.SEVERE, "Tried parsing json from third-party api", parseException);
        } finally {
            Closeables.closeQuietly(inputReader);
        }

        return null;
    }

    public SkinData downloadSkin(UUID ownerUUID) {
        if (mojangDownload) {
            return downloadSkinFromApi(ownerUUID);
        }

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
            logger.log(Level.SEVERE, "Tried downloading skin data from Mojang", ioException);
        } catch (JsonParseException parseException) {
            logger.log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }

    public SkinData downloadSkinFromApi(UUID ownerUUID) {
        //unsigned is needed in order to receive the signature
        String uuidStrip = ownerUUID.toString().replace("-", "");
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(MCAPI_SKIN_URL + uuidStrip).openConnection();
            httpConnection.addRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                McApiProfile profile = gson.fromJson(line, McApiProfile.class);

                ApiPropertiesModel properties = profile.getProperties();
                if (properties != null && properties.getRaw().length > 0) {
                    RawPropertiesModel propertiesModel = properties.getRaw()[0];

                    //base64 encoded skin data
                    String encodedSkin = propertiesModel.getValue();
                    String signature = propertiesModel.getSignature();

                    SkinData skinData = new SkinData(encodedSkin, signature);
                    return skinData;
                }
            }
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, "Tried downloading skin data from Mojang", ioException);
        } catch (JsonParseException parseException) {
            logger.log(Level.SEVERE, "Tried parsing json from Mojang", parseException);
        }

        return null;
    }
}
