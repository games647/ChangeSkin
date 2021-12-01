package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.GameProfile;
import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.core.model.skin.TexturesModel;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import static com.github.games647.changeskin.core.CommonUtil.getConnection;
import static java.util.stream.Collectors.toSet;

public class MojangSkinApi {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s" +
            "?unsigned=false";
    private static final String RATE_LIMIT_MSG = "Mojang's rate-limit reached. The public IPv4 address of this server" +
            " issued more than 600 Name -> UUID requests within 10 minutes. Once those 10 minutes for the" +
            " first ended we could make requests again. In the meanwhile new skins can only be downloaded using the" +
            " UUID directly. If you are using BungeeCord, consider adding a caching server in order to " +
            " prevent multiple spigot servers creating the same requests against Mojang's servers.";

    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private final Pattern validNamePattern = Pattern.compile("^\\w{2,16}$");
    private final Iterator<Proxy> proxies;
    private final Logger logger;

    private final RateLimiter rateLimiter;
    private final Map<UUID, Object> crackedUUID = CommonUtil.buildCache(60, -1);

    private Instant lastRateMsg = Instant.now().minus(10, ChronoUnit.MINUTES);

    public MojangSkinApi(Logger logger, int rateLimit, Collection<HostAndPort> proxies) {
        this.logger = logger;
        this.rateLimiter = new RateLimiter(Duration.ofMinutes(10), Math.max(rateLimit, 600));

        Set<Proxy> proxyBuilder = proxies.stream()
                .map(proxy -> new InetSocketAddress(proxy.getHost(), proxy.getPort()))
                .map(sa -> new Proxy(Type.HTTP, sa))
                .collect(toSet());

        this.proxies = Iterables.cycle(proxyBuilder).iterator();
    }

    public Optional<UUID> getUUID(String playerName) throws NotPremiumException, RateLimitException {
        logger.debug("Making UUID->Name request for {}", playerName);
        if (!validNamePattern.matcher(playerName).matches()) {
            throw new NotPremiumException(playerName);
        }

        try {
            Optional<HttpURLConnection> connection = selectConnection(playerName);
            if (connection.isPresent()) {
                try {
                    return getUUID(connection.get(), playerName);
                } catch (RateLimitException rateLimitEx) {
                    //retry with a proxy if available
                    connection = getProxyConnection(playerName);
                    if (connection.isPresent()) {
                        return getUUID(connection.get(), playerName);
                    }
                }
            }

            if (Duration.between(lastRateMsg, Instant.now()).getSeconds() > 60 * 10) {
                lastRateMsg = Instant.now();
                logger.info(RATE_LIMIT_MSG);
            }

            throw new RateLimitException(playerName);
        } catch (IOException ioEx) {
            logger.error("Tried converting player name: {} to uuid", playerName, ioEx);
        }

        return Optional.empty();
    }

    /**
     * @param playerName query player uuid
     * @return http connection
     * @throws IOException on failure to connect
     */
    private Optional<HttpURLConnection> selectConnection(String playerName) throws IOException {
        if (rateLimiter.tryAcquire()) {
            return Optional.of(getConnection(UUID_URL + playerName));
        } else {
            return getProxyConnection(playerName);
        }
    }

    private Optional<HttpURLConnection> getProxyConnection(String playerName) throws IOException {
        synchronized (proxies) {
            if (proxies.hasNext()) {
                Proxy proxy = proxies.next();
                return Optional.of(getConnection(UUID_URL + playerName, proxy));
            }
        }

        return Optional.empty();
    }

    private Optional<UUID> getUUID(HttpURLConnection connection, String playerName)
            throws IOException, RateLimitException, NotPremiumException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            throw new NotPremiumException(playerName);
        } else if (responseCode == RateLimitException.RATE_LIMIT_ID) {
            throw new RateLimitException(playerName);
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return parseUUID(reader);
            }
        } else {
            printErrorStream(connection, responseCode);
        }

        return Optional.empty();
    }

    private void printErrorStream(HttpURLConnection connection, int responseCode) throws IOException {
        boolean proxy = connection.usingProxy();

        //this necessary, because we cannot access input stream if the response code is something like 404
        try (InputStream in = responseCode < HttpURLConnection.HTTP_BAD_REQUEST ?
                connection.getInputStream() : connection.getErrorStream()) {
            logger.error("Received response: {} for {} using proxy?: {}", responseCode, connection.getURL(), proxy);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                logger.error("Error stream: {}", CharStreams.toString(reader));
            }
        }
    }

    public Optional<SkinModel> downloadSkin(UUID ownerUUID) {
        if (crackedUUID.containsKey(ownerUUID) || ownerUUID == null) {
            return Optional.empty();
        }

        //unsigned is needed in order to receive the signature
        String uuidString = UUIDTypeAdapter.toMojangId(ownerUUID);
        try {
            HttpURLConnection conn = getConnection(String.format(SKIN_URL, uuidString));
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                crackedUUID.put(ownerUUID, new Object());
                return Optional.empty();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
            ) {
                return parseSkinTexture(reader);
            }
        } catch (IOException ex) {
            logger.error("Tried downloading skin data of: {} from Mojang", ownerUUID, ex);
        }

        return Optional.empty();
    }

    private Optional<UUID> parseUUID(Reader reader) {
        GameProfile playerProfile = gson.fromJson(reader, GameProfile.class);
        return Optional.of(playerProfile.getId());
    }

    private Optional<SkinModel> parseSkinTexture(Reader reader) {
        TexturesModel texturesModel = gson.fromJson(reader, TexturesModel.class);

        SkinProperty[] properties = texturesModel.getProperties();
        try {
            if (properties != null && properties.length > 0) {
                SkinProperty propertiesModel = properties[0];

                //base64 encoded skin data
                String encodedSkin = propertiesModel.getValue();
                String signature = propertiesModel.getSignature();

                return Optional.of(SkinModel.createSkinFromEncoded(encodedSkin, signature));
            }
        } catch (Exception ex) {
            logger.error("Failed to parse skin model", ex);
            logger.error(texturesModel.toString());
        }


        return Optional.empty();
    }
}
