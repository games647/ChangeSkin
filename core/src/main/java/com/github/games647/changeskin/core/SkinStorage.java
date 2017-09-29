package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

public class SkinStorage {

    private static final String PREFERENCES_TABLE = "preferences";
    private static final String DATA_TABLE = "skinData";

    private final ChangeSkinCore plugin;
    private final HikariDataSource dataSource;

    private boolean keepColumnPresent;

    public SkinStorage(ChangeSkinCore core, String driver, String host, int port, String databasePath
            , String user, String pass, boolean useSSL) {
        this.plugin = core;

        HikariConfig config = new HikariConfig();
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName(driver);

        ThreadFactory threadFactory = core.getPlugin().getThreadFactory();
        if (threadFactory != null) {
            config.setThreadFactory(threadFactory);
        }

        databasePath = databasePath.replace("{pluginDir}", core.getPlugin().getDataFolder().toString());

        //a try to fix https://www.spigotmc.org/threads/fastlogin.101192/page-26#post-1874647
        Properties properties = new Properties();
        properties.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
        properties.setProperty("useSSL", String.valueOf(useSSL));
        config.setDataSourceProperties(properties);

        String jdbcUrl = "jdbc:";
        if (driver.contains("sqlite")) {
            jdbcUrl += "sqlite" + "://" + databasePath;
            config.setConnectionTestQuery("SELECT 1");
            config.setMaximumPoolSize(1);
        } else {
            jdbcUrl += "mysql" + "://" + host + ':' + port + '/' + databasePath;
        }

        config.setJdbcUrl(jdbcUrl);
        this.dataSource = new HikariDataSource(config);
    }

    public void createTables() throws SQLException {
        try (Connection con = dataSource.getConnection();
            Statement stmt = con.createStatement()) {
            String createDataStmt = "CREATE TABLE IF NOT EXISTS " + DATA_TABLE + " ("
                    + "`SkinID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "`DisplayName` VARCHAR(255), "
                    + "`Timestamp` BIGINT NOT NULL, "
                    + "`UUID` CHAR(36) NOT NULL, "
                    + "`Name` VARCHAR(16) NOT NULL, "
                    + "`SlimModel` BIT DEFAULT 0 NOT NULL, "
                    + "`SkinURL` VARCHAR(255) NOT NULL, "
                    + "`CapeURL` VARCHAR(255), "
                    + "`Signature` BLOB NOT NULL, "
                    + "INDEX(`Name`, `UUID`)"
                    + ')';

            String createPreferencesStmt = "CREATE TABLE IF NOT EXISTS " + PREFERENCES_TABLE + " ("
                    + "`UserID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "`UUID` CHAR(36) NOT NULL, "
                    + "`TargetSkin` INTEGER NOT NULL, "
                    + "`KeepSkin` BIT NOT NULL DEFAULT 1, "
                    + "UNIQUE (`UUID`), "
                    + "FOREIGN KEY (`TargetSkin`) "
                    + "     REFERENCES " + DATA_TABLE + " (`SkinID`) "
                    + "     ON DELETE CASCADE "
                    + ')';

            if (dataSource.getJdbcUrl().contains("sqlite")) {
                createPreferencesStmt = createPreferencesStmt.replace("AUTO_INCREMENT", "AUTOINCREMENT");
                createDataStmt = createDataStmt.replace("AUTO_INCREMENT", "AUTOINCREMENT")
                        .replace(", INDEX(`Name`, `UUID`)", "");
            }

            stmt.executeUpdate(createDataStmt);
            stmt.executeUpdate(createPreferencesStmt);

            try (ResultSet testResult = stmt.executeQuery("SELECT * FROM " + DATA_TABLE + " Limit 1")) {
                ResultSetMetaData meta = testResult.getMetaData();
                for (int i = 1; i < meta.getColumnCount() + 1; i++) {
                    if ("KeepSkin".equals(meta.getColumnName(i))) {
                        keepColumnPresent = true;
                        break;
                    }
                }
            }
        }
    }

    public UserPreference getPreferences(UUID uuid) {
        try (Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT SkinId, Timestamp, "
                    + DATA_TABLE + ".UUID, Name, SlimModel, SkinUrl, CapeUrl, Signature, " + PREFERENCES_TABLE + ".*"
                    + " FROM " + PREFERENCES_TABLE
                    + " JOIN " + DATA_TABLE + " ON " + PREFERENCES_TABLE + ".TargetSkin=" + DATA_TABLE + ".SkinID"
                    + " WHERE " + PREFERENCES_TABLE + ".UUID=? LIMIT 1")) {
            stmt.setString(1, uuid.toString().replace("-", ""));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    SkinData skinData = parseSkinData(resultSet);
                    boolean keepSkin = false;
                    if (keepColumnPresent) {
                        keepSkin = resultSet.getBoolean(11);
                    }

                    return new UserPreference(uuid, skinData, keepSkin);
                } else {
                    return new UserPreference(uuid);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Failed to query preferences", sqlEx);
        }

        return null;
    }

    public SkinData getSkin(int targetSkinId) {
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
            plugin.getLogger().error("Failed to query skin data from row id", sqlEx);
        }

        return null;
    }

    public SkinData getSkin(UUID skinUUID) {
        try (Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT SkinId, Timestamp, UUID, Name, " +
                    "SlimModel, SkinUrl, CapeUrl, Signature FROM " + DATA_TABLE
                    + " WHERE UUID=? ORDER BY Timestamp DESC LIMIT 1")) {
            stmt.setString(1, skinUUID.toString().replace("-", ""));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return parseSkinData(resultSet);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Failed to query skin data from uuid", sqlEx);
        }

        return null;
    }

    public void save(UserPreference preferences) {
        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin != null && targetSkin.getSkinId() == -1) {
            throw new IllegalArgumentException("Tried saving preferences without skin");
        }

        try (Connection con = dataSource.getConnection()) {
            if (targetSkin == null) {
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM "
                        + PREFERENCES_TABLE + " WHERE UUID=?")) {
                    stmt.setString(1, preferences.getUuid().toString().replace("-", ""));
                    stmt.executeUpdate();
                }
            } else {
                String insertQuery = "REPLACE INTO " + PREFERENCES_TABLE + " (UUID, TargetSkin) VALUES (?, ?)";
                if (keepColumnPresent) {
                    insertQuery = "REPLACE INTO " + PREFERENCES_TABLE + " (UUID, TargetSkin, KeepSkin) VALUES (?, ?, ?)";
                }

                try (PreparedStatement stmt = con.prepareStatement(insertQuery)) {
                    stmt.setString(1, preferences.getUuid().toString().replace("-", ""));
                    stmt.setInt(2, targetSkin.getSkinId());
                    if (keepColumnPresent) {
                        stmt.setBoolean(3, preferences.isKeepSkin());
                    }

                    stmt.executeUpdate();
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Failed to save preferences", sqlEx);
        }
    }

    public boolean save(SkinData skinData) {
        if (skinData == null) {
            return false;
        }

        if (skinData.getSkinId() != -1) {
            //skin already set
            return true;
        }

        String skinUrl = skinData.getSkinURL();

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + DATA_TABLE
                + " (Timestamp, UUID, Name, SlimModel, SkinURL, CapeURL, Signature) VALUES"
                + " (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, skinData.getTimestamp());
            stmt.setString(2, skinData.getUuid().toString().replace("-", ""));
            stmt.setString(3, skinData.getName());
            stmt.setBoolean(4, skinData.isSlimModel());
            stmt.setString(5, skinUrl);
            stmt.setString(6, skinData.getCapeURL());
            stmt.setBytes(7, Base64.getDecoder().decode(skinData.getEncodedSignature()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys != null && generatedKeys.next()) {
                    skinData.setSkinId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Failed to query skin data", sqlEx);
        }

        return false;
    }

    private SkinData parseSkinData(ResultSet resultSet) throws SQLException {
        int skinId = resultSet.getInt(1);
        long timestamp = resultSet.getLong(2);
        UUID uuid = CommonUtil.parseId(resultSet.getString(3));
        String name = resultSet.getString(4);

        boolean slimModel = resultSet.getBoolean(5);

        String skinUrl = resultSet.getString(6);
        String capeUrl = resultSet.getString(7);

        byte[] signature = resultSet.getBytes(8);
        return new SkinData(skinId, timestamp, uuid, name, slimModel, skinUrl, capeUrl, signature);
    }
}
