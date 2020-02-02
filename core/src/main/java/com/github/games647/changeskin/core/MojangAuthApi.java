package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.model.auth.AuthRequest;
import com.github.games647.changeskin.core.model.auth.AuthResponse;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

public class MojangAuthApi {

    private static final String CHANGE_SKIN_URL = "https://api.mojang.com/user/profile/<uuid>/skin";
    private static final String AUTH_URL = "https://authserver.mojang.com/authenticate";

    private final Logger logger;
    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    public MojangAuthApi(Logger logger) {
        this.logger = logger;
    }

    public Optional<Account> authenticate(String email, String password) {
        try {
            HttpURLConnection httpConnection = CommonUtil.getConnection(AUTH_URL);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.append(gson.toJson(new AuthRequest(email, password)));
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpConnection.getInputStream(), StandardCharsets.UTF_8))) {
                AuthResponse authResponse = gson.fromJson(reader, AuthResponse.class);
                return Optional.of(new Account(authResponse.getSelectedProfile(), authResponse.getAccessToken()));
            }
        } catch (IOException ex) {
            logger.error("Failed to authenticate user: {}", email, ex);
        }

        return Optional.empty();
    }

    public void changeSkin(UUID ownerId, String accessToken, String sourceUrl, boolean slimModel) {
        String url = CHANGE_SKIN_URL.replace("<uuid>", UUIDTypeAdapter.toMojangId(ownerId));

        try {
            HttpURLConnection httpConnection = CommonUtil.getConnection(url);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            httpConnection.addRequestProperty("Authorization", "Bearer " + accessToken);
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write("model=");
                if (slimModel) {
                    writer.write("slim");
                }

                writer.write("&url=" + UrlEscapers.urlPathSegmentEscaper().escape(sourceUrl));
            }

            logger.debug("Response code for uploading {}", httpConnection.getResponseCode());
        } catch (IOException ioEx) {
            logger.error("Tried uploading {}'s skin data {} to Mojang {}", ownerId, sourceUrl, url, ioEx);
        }
    }
}
