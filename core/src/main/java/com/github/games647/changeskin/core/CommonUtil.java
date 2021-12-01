package com.github.games647.changeskin.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.jul.JDK14LoggerAdapter;

public class CommonUtil {

    private static final int TIMEOUT = 3000;
    private static final String USER_AGENT = "ChangeSkin-Bukkit-Plugin";

    private static final char COLOR_CHAR = '&';
    private static final char TRANSLATED_CHAR = '§';

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
            public V load(K key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }).asMap();
    }

    public static String translateColorCodes(String rawMessage) {
        char[] chars = rawMessage.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = TRANSLATED_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }

        return new String(chars);
    }

    public static HttpURLConnection getConnection(String url) throws IOException {
        return getConnection(url, Proxy.NO_PROXY);
    }

    public static HttpURLConnection getConnection(String url, Proxy proxy) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection(proxy);

        httpConnection.setConnectTimeout(TIMEOUT);
        httpConnection.setReadTimeout(2 * TIMEOUT);

        httpConnection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        httpConnection.setRequestProperty(HttpHeaders.USER_AGENT, USER_AGENT);
        return httpConnection;
    }

    public static Logger createLoggerFromJDK(java.util.logging.Logger parent) {
        try {
            parent.setLevel(Level.ALL);

            Class<JDK14LoggerAdapter> adapterClass = JDK14LoggerAdapter.class;
            Constructor<JDK14LoggerAdapter> cons = adapterClass.getDeclaredConstructor(java.util.logging.Logger.class);
            cons.setAccessible(true);
            return cons.newInstance(parent);
        } catch (ReflectiveOperationException reflectEx) {
            parent.log(Level.WARNING, "Cannot create slf4j logging adapter", reflectEx);
            parent.log(Level.WARNING, "Creating logger instance manually...");
            return LoggerFactory.getLogger(parent.getName());
        }
    }

    private CommonUtil() {
        //Utility class
    }
}
