package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.ApiPropertiesModel;
import com.github.games647.changeskin.core.model.McApiProfile;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.RawPropertiesModel;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.skin.PropertiesModel;
import com.github.games647.changeskin.core.model.mojang.skin.TexturesModel;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.games647.changeskin.core.ChangeSkinCore.getConnection;

public class MojangSkinApi {

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String MCAPI_SKIN_URL = "https://mcapi.de/api/user/";

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private static final int RATE_LIMIT_ID = 429;

    private final Gson gson = new Gson();

    private final Iterator<Proxy> proxies;
    private final ConcurrentMap<Object, Object> requests;
    private final Logger logger;
    private final int rateLimit;
    private final boolean mojangDownload;

    private final ConcurrentMap<UUID, Object> crackedUUID = ChangeSkinCore.buildCache(60, -1);

    private long lastRateLimit;

    public MojangSkinApi(ConcurrentMap<Object, Object> requests, Logger logger, int rateLimit, boolean mojangDownload
            , Map<String, Integer> proxies) {
        this.requests = requests;
        this.rateLimit = Math.max(rateLimit, 600);
        this.logger = logger;
        this.mojangDownload = mojangDownload;

        List<Proxy> proxyBuilder = Lists.newArrayList();
        for (Entry<String, Integer> proxy : proxies.entrySet()) {
            proxyBuilder.add(new Proxy(Type.HTTP, new InetSocketAddress(proxy.getKey(), proxy.getValue())));
        }

        this.proxies = Iterables.cycle(proxyBuilder).iterator();
    }

    public UUID getUUID(String playerName) throws NotPremiumException, RateLimitException {
        logger.log(Level.FINE, "Making UUID->Name request for {0}", playerName);
        if (!playerName.matches(VALID_USERNAME)) {
            throw new NotPremiumException(playerName);
        }

        BufferedReader reader = null;
        try {
            HttpURLConnection connection;
            if (requests.size() >= rateLimit || System.currentTimeMillis() - lastRateLimit < 1_000 * 60 * 10) {
                synchronized (proxies) {
                    if (proxies.hasNext()) {
                        connection = getConnection(UUID_URL + playerName, proxies.next());
                    } else {
                        return null;
                    }
                }
            } else {
                requests.put(new Object(), new Object());
                connection = getConnection(UUID_URL + playerName);
            }


            if (connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                throw new NotPremiumException(playerName);
            } else if (connection.getResponseCode() == RATE_LIMIT_ID) {
                logger.info("RATE_LIMIT REACHED");
                lastRateLimit = System.currentTimeMillis();
                if (!connection.usingProxy()) {
                    return getUUID(playerName);
                } else {
                    return null;
                }
            }

            InputStreamReader inputReader = new InputStreamReader(connection.getInputStream());
            reader = new BufferedReader(inputReader);
            String line = reader.readLine();
            if (line != null && !"null".equals(line)) {
                PlayerProfile playerProfile = gson.fromJson(line, PlayerProfile.class);
                String id = playerProfile.getId();
                return ChangeSkinCore.parseId(id);
            }
        } catch (IOException ioEx) {
            logger.log(Level.SEVERE, "Tried converting player name to uuid", ioEx);
        } finally {
            ChangeSkinCore.closeQuietly(reader, logger);
        }

        return null;
    }

    public SkinData downloadSkin(UUID ownerUUID) {
        if (crackedUUID.containsKey(ownerUUID)) {
            return null;
        }

        if (mojangDownload) {
            return downloadSkinFromApi(ownerUUID);
        }

        //unsigned is needed in order to receive the signature
        String uuidString = ownerUUID.toString().replace("-", "") + "?unsigned=false";
        try {
            HttpURLConnection httpConnection = getConnection(SKIN_URL + uuidString);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line = reader.readLine();
            if (line == null || "null".equals(line)) {
                crackedUUID.put(ownerUUID, new Object());
            } else {
                TexturesModel texturesModel = gson.fromJson(line, TexturesModel.class);

                PropertiesModel[] properties = texturesModel.getProperties();
                if (properties != null && properties.length > 0) {
                    PropertiesModel propertiesModel = properties[0];

                    //base64 encoded skin data
                    String encodedSkin = propertiesModel.getValue();
                    String signature = propertiesModel.getSignature();

                    return new SkinData(encodedSkin, signature);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Tried downloading skin data from Mojang", ex);
        }

        return null;
    }

    public SkinData downloadSkinFromApi(UUID ownerUUID) {
        //unsigned is needed in order to receive the signature
        String uuidStrip = ownerUUID.toString().replace("-", "");
        try {
            HttpURLConnection httpConnection = getConnection(MCAPI_SKIN_URL + uuidStrip);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            McApiProfile profile = gson.fromJson(reader.readLine(), McApiProfile.class);

            ApiPropertiesModel properties = profile.getProperties();
            if (properties != null && properties.getRaw().length > 0) {
                RawPropertiesModel propertiesModel = properties.getRaw()[0];

                //base64 encoded skin data
                String encodedSkin = propertiesModel.getValue();
                String signature = propertiesModel.getSignature();

                return new SkinData(encodedSkin, signature);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Tried downloading skin data from Mojang", ex);
        }

        return null;
    }
}
