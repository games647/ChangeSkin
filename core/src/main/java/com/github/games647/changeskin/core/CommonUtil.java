package com.github.games647.changeskin.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    private static final int TIMEOUT = 3000;
    private static final String USER_AGENT = "ChangeSkin-Bukkit-Plugin";

    public static UUID parseId(String withoutDashes) {
        return UUID.fromString(withoutDashes.substring(0, 8)
                + '-' + withoutDashes.substring(8, 12)
                + '-' + withoutDashes.substring(12, 16)
                + '-' + withoutDashes.substring(16, 20)
                + '-' + withoutDashes.substring(20, 32));
    }

    public static <K, V> ConcurrentMap<K, V> buildCache(int seconds, int maxSize) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (seconds > 0) {
            builder.expireAfterWrite(seconds, TimeUnit.SECONDS);
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

    public static HttpURLConnection getConnection(String url) throws IOException {
        return getConnection(url, Proxy.NO_PROXY);
    }

    public static HttpURLConnection getConnection(String url, Proxy proxy) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection(proxy);
        httpConnection.setConnectTimeout(TIMEOUT);
        httpConnection.setReadTimeout(2 * TIMEOUT);
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        return httpConnection;
    }

    private CommonUtil() {
        //Utility class
    }
}
