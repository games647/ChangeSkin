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

    /**
     * This creates a SLF4J logger. In the process it initializes the SLF4J service provider. This method looks
     * for the provider in the plugin jar instead of in the server jar when creating a Logger. The provider is only
     * initialized once, so this method should be called early.
     *
     * The provider is bound to the service class `SLF4JServiceProvider`. Relocating this class makes it available
     * for exclusive own usage. Other dependencies will use the relocated service too, and therefore will find the
     * initialized provider.
     *
     * @param parent JDK logger
     * @return slf4j logger
     */
    public static Logger initializeLoggerService(java.util.logging.Logger parent) {
        // set the class loader to the plugin one to find our own SLF4J provider in the plugin jar and not in the global
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();

        ClassLoader pluginLoader = CommonUtil.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(pluginLoader);

        // Trigger provider search
        LoggerFactory.getLogger(parent.getName()).info("Initialize logging service");
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
        } finally {
            // restore previous class loader
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }


    private CommonUtil() {
        //Utility class
    }
}
