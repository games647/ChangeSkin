package com.github.games647.changeskin.core;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;

public abstract class LocaleManager<R> {

    private static final String LANG_FOLDER = "lang";
    private static final String EXT = ".json";

    private final Logger logger;
    private final Path dataFolder;

    private final Gson gson = new Gson();
    private final Map<String, LocaleTable> locales = new ConcurrentHashMap<>();
    private final LocaleTable defaultLocale = new LocaleTable(Locale.ENGLISH);

    public LocaleManager(Logger logger, Path dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;

        locales.put(defaultLocale.getLocale().getLanguage(), defaultLocale);
    }

    public void loadMessages() {
        //default language file
        fillTable(defaultLocale, defaultLocale.getLocale().getLanguage() + EXT);

        Path folder = dataFolder.resolve(LANG_FOLDER);
        try (Stream<Path> fileStream = Files.walk(folder, 1)) {
            fileStream.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(EXT))
                    .forEach(file -> {
                        String baseName = file.getFileName().toString().replace(EXT, "");
                        Locale locale = new Locale.Builder().setLanguage(baseName).build();

                        LocaleTable table = new LocaleTable(locale);
                        locales.put(locale.getLanguage(), table);
                        fillTable(table, file.getFileName().toString());
                    });
        } catch (IOException ioEx) {
            logger.error("Cannot lookup folder contents for additional language files", ioEx);
        }
    }

    private void fillTable(LocaleTable table, String fileName) {
        URL defaultUrl = getClass().getClassLoader().getResource(LANG_FOLDER + '/' + fileName);
        if (defaultUrl != null) {
            try (Reader reader = Resources.asCharSource(defaultUrl, StandardCharsets.UTF_8).openBufferedStream()) {
                fillTable(table, reader);
            } catch (IOException ioEx) {
                logger.warn("Failed to fillTable default language file", ioEx);
            }
        }

        Path langFile = dataFolder.resolve(LANG_FOLDER + '/' + fileName);
        if (Files.exists(langFile)) {
            try (BufferedReader reader = Files.newBufferedReader(langFile)) {
                fillTable(table, reader);
            } catch (IOException ioEx) {
                logger.warn("Failed to fillTable language file", ioEx);
            }
        }
    }

    private void fillTable(LocaleTable table, Reader reader) {
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        for (Entry<String, JsonElement> element : jsonObject.entrySet()) {
            JsonElement value = element.getValue();

            String message = "";
            if (value.isJsonPrimitive()) {
                message = value.getAsString();
            } else if (value.isJsonObject()) {
                message = gson.toJson(value);
            }

            String colored = CommonUtil.translateColorCodes(message);
            table.add(element.getKey(), colored.replace("/newline", "\n"));
        }
    }

    public void sendMessage(R receiver, String messageKey) {
        if (receiver != null) {
            String json = getLocalizedMessage(receiver, messageKey);
            if (json != null && !json.isEmpty()) {
                sendLocalizedMessage(receiver, json);
            }
        }
    }

    public String getLocalizedMessage(R receiver, String messageKey) {
        Locale locale = getLocale(receiver);
        return locales.getOrDefault(locale.getLanguage(), defaultLocale).getMessage(messageKey);
    }

    public abstract Locale getLocale(R receiver);

    protected abstract void sendLocalizedMessage(R receiver, String json);

    private class LocaleTable {

        private final Locale locale;
        private final Map<String, String> messages = new ConcurrentHashMap<>();

        public LocaleTable(Locale locale) {
            this.locale = locale;
        }

        public void add(String key, String msg) {
            messages.put(key, msg);
        }

        public String getMessage(String key) {
            return messages.get(key);
        }

        public Locale getLocale() {
            return locale;
        }
    }
}
