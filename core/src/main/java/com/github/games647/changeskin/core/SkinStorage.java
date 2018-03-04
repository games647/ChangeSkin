package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.StoredSkin;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.craftapi.UUIDAdapter;
import com.github.games647.craftapi.model.skin.Texture;
import com.github.games647.craftapi.model.skin.TextureType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import static com.github.games647.craftapi.model.skin.TextureType.CAPE;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SkinStorage {

    private static final String USER_TABLE = "preferences";
    private static final String DATA_TABLE = "skinData";

    private final ChangeSkinCore core;
    private final HikariDataSource dataSource;

    public SkinStorage(ChangeSkinCore core, String driver, String host, int port, String database
            , String user, String pass, boolean useSSL) {
        this.core = core;

        HikariConfig config = new HikariConfig();
        config.setPoolName(core.getPlugin().getName());

        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName(driver);

        ThreadFactory threadFactory = core.getPlugin().getThreadFactory();
        if (threadFactory != null) {
            config.setThreadFactory(threadFactory);
        }

        Properties properties = new Properties();

        String jdbcUrl = "jdbc:";
        if (driver.contains("sqlite")) {
            String folderPath = core.getPlugin().getPluginFolder().toAbsolutePath().toString();
            database = database.replace("{pluginDir}", folderPath);

            jdbcUrl += "sqlite://" + database;
            config.setConnectionTestQuery("SELECT 1");
            config.setMaximumPoolSize(1);

            //a try to fix https://www.spigotmc.org/threads/fastlogin.101192/page-26#post-1874647
            properties.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
        } else {
            jdbcUrl += "mysql://" + host + ':' + port + '/' + database;
            properties.setProperty("useSSL", String.valueOf(useSSL));
        }

        config.setJdbcUrl(jdbcUrl);
        config.setDataSourceProperties(properties);
        this.dataSource = new HikariDataSource(config);
    }

    public void createTables() throws SQLException {
        try (InputStream in = getClass().getResourceAsStream("/create.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;

                builder.append(line);
                if (line.endsWith(";")) {
                    String sql = builder.toString();
                    if (dataSource.getJdbcUrl().contains("sqlite")) {
                        sql = sql.replace("AUTO_INCREMENT", "AUTOINCREMENT");
                    }

                    stmt.addBatch(sql);
                    builder = new StringBuilder();
                }
            }

            stmt.executeBatch();
        } catch (IOException ioEx) {
            core.getLogger().error("Failed to load migration file", ioEx);
        }
    }

    public UserPreference getPreferences(UUID uuid) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT SkinId, Timestamp, "
                     + DATA_TABLE + ".UUID, Name, SlimModel, SkinUrl, CapeUrl, Signature, " + USER_TABLE + ".*"
                     + " FROM " + USER_TABLE
                     + " LEFT JOIN " + DATA_TABLE + " ON " + USER_TABLE + ".TargetSkin=" + DATA_TABLE + ".SkinID"
                     + " WHERE " + USER_TABLE + ".UUID=? LIMIT 1")) {
            stmt.setString(1, UUIDAdapter.toMojangId(uuid));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    int prefId = resultSet.getInt(9);

                    StoredSkin skinData = null;
                    if (resultSet.getObject(1)  != null) {
                        skinData = parseSkinData(resultSet);
                    }

                    boolean keepSkin = resultSet.getBoolean(12);
                    return new UserPreference(prefId, uuid, skinData, keepSkin);
                } else {
                    return new UserPreference(uuid);
                }
            }
        } catch (SQLException sqlEx) {
            core.getLogger().error("Failed to query preferences {}", uuid, sqlEx);
        }

        return null;
    }

    public StoredSkin getSkin(int targetSkinId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT SkinId, Timestamp, UUID, Name, " +
                     "SlimModel, SkinUrl, CapeUrl, Signature FROM " + DATA_TABLE + " WHERE SkinID=? LIMIT 1")) {
            stmt.setInt(1, targetSkinId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return parseSkinData(resultSet);
                }
            }
        } catch (SQLException sqlEx) {
            core.getLogger().error("Failed to query skin data from row id: {}", targetSkinId, sqlEx);
        }

        return null;
    }

    public StoredSkin getSkin(UUID skinUUID) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT SkinId, Timestamp, UUID, Name, " +
                     "SlimModel, SkinUrl, CapeUrl, Signature FROM " + DATA_TABLE
                     + " WHERE UUID=? ORDER BY Timestamp DESC LIMIT 1")) {
            stmt.setString(1, UUIDAdapter.toMojangId(skinUUID));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return parseSkinData(resultSet);
                }
            }
        } catch (SQLException sqlEx) {
            core.getLogger().error("Failed to query skin data from uuid: {}", skinUUID, sqlEx);
        }

        return null;
    }

    public void save(UserPreference preferences) {
        StoredSkin targetSkin = preferences.getTargetSkin();
        if (targetSkin != null && targetSkin.getSkinId() == -1) {
            throw new IllegalArgumentException("Tried saving preferences without skin");
        }

        try (Connection con = dataSource.getConnection()) {
            if (preferences.isSaved()) {
                try (PreparedStatement stmt = con.prepareStatement("UPDATE " + USER_TABLE
                        + " SET TargetSkin=? WHERE UserID=?")) {
                    stmt.setInt(1, targetSkin == null ? -1 : targetSkin.getSkinId());
                    stmt.setInt(2, preferences.getId());
                    stmt.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO " + USER_TABLE + " (UUID, TargetSkin, KeepSkin) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement stmt = con.prepareStatement(insertQuery, RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, UUIDAdapter.toMojangId(preferences.getUuid()));
                    stmt.setInt(2, targetSkin == null ? -1 : targetSkin.getSkinId());
                    stmt.setBoolean(3, preferences.isKeepSkin());

                    stmt.executeUpdate();

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys != null && generatedKeys.next()) {
                            preferences.setId(generatedKeys.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException sqlEx) {
            core.getLogger().error("Failed to save preferences for: {}", preferences, sqlEx);
        }
    }

    public boolean save(StoredSkin skinData) {
        if (skinData == null) {
            return false;
        }

        if (skinData.isSaved()) {
            //skin already saved
            return true;
        }

        Optional<Texture> skinTexture = skinData.getTexture(TextureType.SKIN);
        String skinUrl = skinTexture.map(Texture::getShortUrl).orElse("");
        boolean slimModel = skinTexture.map(Texture::getMetadata).map(Optional::isPresent).orElse(false);

        Optional<Texture> capeTexture = skinData.getTexture(CAPE);
        String capeUrl = capeTexture.map(Texture::getShortUrl).orElse("");

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + DATA_TABLE
                     + " (Timestamp, UUID, Name, SlimModel, SkinURL, CapeURL, Signature) VALUES"
                     + " (?, ?, ?, ?, ?, ?, ?)", RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.from(skinData.getTimestamp()));
            stmt.setString(2, UUIDAdapter.toMojangId(skinData.getOwnerId()));
            stmt.setString(3, skinData.getOwnerName());
            stmt.setBoolean(4, slimModel);
            stmt.setString(5, skinUrl);
            stmt.setString(6, capeUrl);
            stmt.setBytes(7, Base64.getDecoder().decode(skinData.getSignature()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys != null && generatedKeys.next()) {
                    skinData.setSkinId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException sqlEx) {
            core.getLogger().error("Failed to query skin data: {}", skinData, sqlEx);
        }

        return false;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private StoredSkin parseSkinData(ResultSet resultSet) throws SQLException {
        int skinId = resultSet.getInt(1);
        Instant timestamp = resultSet.getTimestamp(2).toInstant();
        UUID uuid = UUIDAdapter.parseId(resultSet.getString(3));
        String name = resultSet.getString(4);

        boolean slimModel = resultSet.getBoolean(5);

        String skinUrl = resultSet.getString(6);
        String capeUrl = resultSet.getString(7);

        byte[] signature = resultSet.getBytes(8);
        return new StoredSkin(skinId, timestamp, uuid, name, slimModel, skinUrl, capeUrl, signature);
    }
}
