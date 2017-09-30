package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.GameProfile;
import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperties;
import com.github.games647.changeskin.core.model.skin.TexturesModel;
import com.google.common.collect.Iterables;
import com.google.common.net.HostAndPort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static com.github.games647.changeskin.core.CommonUtil.getConnection;

public class MojangSkinApi {

    private static final int RATE_LIMIT_ID = 429;
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s" +
            "?unsigned=false";

    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private final Pattern validNamePattern = Pattern.compile("^\\w{2,16}$");
    private final Iterator<Proxy> proxies;
    private final Logger logger;
    private final int rateLimit;

    private final Map<Object, Object> requests = CommonUtil.buildCache(10, -1);
    private final Map<UUID, Object> crackedUUID = CommonUtil.buildCache(60, -1);

    private long lastRateLimit;

    public MojangSkinApi(Logger logger, int rateLimit, Collection<HostAndPort> proxies) {
        this.rateLimit = Math.max(rateLimit, 600);
        this.logger = logger;

        List<Proxy> proxyBuilder = proxies.stream()
                .map(proxy -> {
                    InetSocketAddress sa = new InetSocketAddress(proxy.getHostText(), proxy.getPort());
                    return new Proxy(Type.HTTP, sa);
                }).collect(Collectors.toList());

        this.proxies = Iterables.cycle(proxyBuilder).iterator();
    }

    public Optional<UUID> getUUID(String playerName) throws NotPremiumException, RateLimitException {
        logger.debug("Making UUID->Name request for {}", playerName);
        if (!validNamePattern.matcher(playerName).matches()) {
            throw new NotPremiumException(playerName);
        }

        try {
            HttpURLConnection connection;
            if (requests.size() >= rateLimit || System.currentTimeMillis() - lastRateLimit < 1_000 * 60 * 10) {
                synchronized (proxies) {
                    if (proxies.hasNext()) {
                        connection = getConnection(UUID_URL + playerName, proxies.next());
                    } else {
                        return Optional.empty();
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
                    throw new RateLimitException("Rate-Limit hit on request name->uuid of " + playerName);
                }
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                GameProfile playerProfile = gson.fromJson(reader, GameProfile.class);
                return Optional.of(playerProfile.getId());
            }
        } catch (IOException ioEx) {
            logger.error("Tried converting player name to uuid", ioEx);
        }

        return Optional.empty();
    }

    public Optional<SkinModel> downloadSkin(UUID ownerUUID) {
        if (crackedUUID.containsKey(ownerUUID)) {
            return Optional.empty();
        }

        //unsigned is needed in order to receive the signature
        String uuidString = ownerUUID.toString().replace("-", "");
        try {
            HttpURLConnection httpConnection = getConnection(String.format(SKIN_URL, uuidString));
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                crackedUUID.put(ownerUUID, new Object());
                return Optional.empty();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                TexturesModel texturesModel = gson.fromJson(reader, TexturesModel.class);

                SkinProperties[] properties = texturesModel.getProperties();
                if (properties != null && properties.length > 0) {
                    SkinProperties propertiesModel = properties[0];

                    //base64 encoded skin data
                    String encodedSkin = propertiesModel.getValue();
                    String signature = propertiesModel.getSignature();

                    return Optional.of(SkinModel.createSkinFromEncoded(encodedSkin, signature));
                }
            }
        } catch (Exception ex) {
            logger.error("Tried downloading skin data from Mojang", ex);
        }

        return Optional.empty();
    }
}
