// package com.github.games647.changeskin.core;
//
// import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
// import com.github.games647.changeskin.core.model.auth.Account;
// import com.github.games647.changeskin.core.model.auth.AuthRequest;
// import com.github.games647.changeskin.core.model.auth.AuthResponse;
// import com.google.common.net.UrlEscapers;
// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
//
// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.OutputStreamWriter;
// import java.net.HttpURLConnection;
// import java.nio.charset.StandardCharsets;
// import java.util.Optional;
// import java.util.UUID;
//
// import org.slf4j.Logger;
//
// public class MojangAuthApi {
//
//     private static final String CHANGE_SKIN_URL = "https://api.mojang.com/user/profile/<uuid>/skin";
//
//     private final Logger logger;
//
//     public MojangAuthApi(Logger logger) {
//         this.logger = logger;
//     }
//
//     public void changeSkin(UUID ownerId, UUID accessToken, String sourceUrl, boolean slimModel) {
//         String url = CHANGE_SKIN_URL.replace("<uuid>", UUIDTypeAdapter.toMojangId(ownerId));
//
//         try {
//             HttpURLConnection httpConnection = CommonUtil.getConnection(url);
//             httpConnection.setRequestMethod("POST");
//             httpConnection.setDoOutput(true);
//
//             httpConnection.addRequestProperty("Authorization", "Bearer " + UUIDTypeAdapter.toMojangId(accessToken));
//             try (BufferedWriter writer = new BufferedWriter(
//                     new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8))) {
//                 writer.write("model=");
//                 if (slimModel) {
//                     writer.write("slim");
//                 }
//
//                 writer.write("&url=" + UrlEscapers.urlPathSegmentEscaper().escape(sourceUrl));
//             }
//
//             logger.debug("Response code for uploading {}", httpConnection.getResponseCode());
//         } catch (IOException ioEx) {
//             logger.error("Tried uploading {}'s skin data {} to Mojang {}", ownerId, sourceUrl, url, ioEx);
//         }
//     }
// }
