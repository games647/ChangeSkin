package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.io.BaseEncoding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

public class SkinStorage {

    private static final String PREFERENCES_TABLE = "preferences";
    private static final String DATA_TABLE = "skinData";

    private final String driver;
    private final String jdbcUrl;
    private final String username;
    private final String pass;

    private final ChangeSkinCore plugin;

    public SkinStorage(ChangeSkinCore plugin
            , String driver, String host, int port, String databasePath, String user, String pass) {
        this.plugin = plugin;

        this.driver = driver;
        databasePath = databasePath.replace("{pluginDir}", plugin.getDataFolder().getAbsolutePath());

        String url = "jdbc:";
        if (driver.contains("sqlite")) {
            url += "sqlite" + "://" + databasePath;
        } else {
            url += "mysql" + "://" + host + ':' + port + '/' + databasePath;
        }

        this.jdbcUrl = url;

        this.username = user;
        this.pass = pass;
    }

    public void createTables() throws ClassNotFoundException, SQLException {
        //load the driver
        Class.forName(driver);

        Connection con = null;
        Statement stmt = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);
            stmt = con.createStatement();
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
                    + ")";

            String createPreferencesStmt = "CREATE TABLE IF NOT EXISTS " + PREFERENCES_TABLE + " ("
                    + "`UserID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "`UUID` CHAR(36) NOT NULL, "
                    + "`TargetSkin` INTEGER NOT NULL, "
                    + "UNIQUE (`UUID`), "
                    + "FOREIGN KEY (`TargetSkin`) "
                    + "     REFERENCES " + DATA_TABLE + " (`SkinID`) "
                    + "     ON DELETE CASCADE "
                    + ")";

            if (jdbcUrl.contains("sqlite")) {
                createPreferencesStmt = createPreferencesStmt.replace("AUTO_INCREMENT", "AUTOINCREMENT");
                createDataStmt = createDataStmt.replace("AUTO_INCREMENT", "AUTOINCREMENT")
                        .replace(", INDEX(`Name`, `UUID`)", "");
            }

            stmt.executeUpdate(createDataStmt);
            stmt.executeUpdate(createPreferencesStmt);
            stmt.executeUpdate("UPDATE " + DATA_TABLE + " SET "
                    + "`SkinURL`=REPLACE(`SkinURL`, 'http://textures.minecraft.net/texture/', ''), "
                    + "`CapeURL`=REPLACE(`CapeURL`, 'http://textures.minecraft.net/texture/', '')");
        } finally {
            closeQuietly(stmt);
            closeQuietly(con);
        }
    }

    public UserPreference getPreferences(UUID uuid) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);

            stmt = con.prepareStatement("SELECT TargetSkin FROM " + PREFERENCES_TABLE + " WHERE UUID=? LIMIT 1");

            stmt.setString(1, uuid.toString().replace("-", ""));
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int targetSkinId = resultSet.getInt(1);
                SkinData skinData = getSkin(targetSkinId);
                return new UserPreference(uuid, skinData);
            } else {
                return new UserPreference(uuid);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query preferences", sqlEx);
        } finally {
            closeQuietly(resultSet);
            closeQuietly(stmt);
            closeQuietly(con);
        }

        return null;
    }

    public SkinData getSkin(int targetSkinId) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);

            stmt = con.prepareStatement("SELECT SkinId, Timestamp, UUID, Name, SlimModel, SkinUrl, CapeUrl, Signature "
                    + "FROM " + DATA_TABLE + " WHERE SkinID=? LIMIT 1");
            stmt.setInt(1, targetSkinId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return parseSkinData(resultSet);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query skin data from row id", sqlEx);
        } finally {
            closeQuietly(resultSet);
            closeQuietly(stmt);
            closeQuietly(con);
        }

        return null;
    }

    public SkinData getSkin(UUID skinUUID) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);

            stmt = con.prepareStatement("SELECT SkinId, Timestamp, UUID, Name, SlimModel, SkinUrl, CapeUrl, Signature "
                    + "FROM " + DATA_TABLE + " WHERE UUID=? LIMIT 1");
            stmt.setString(1, skinUUID.toString().replace("-", ""));

            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return parseSkinData(resultSet);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query skin data from uuid", sqlEx);
        } finally {
            closeQuietly(resultSet);
            closeQuietly(stmt);
            closeQuietly(con);
        }

        return null;
    }

    public void save(UserPreference preferences) {
        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin != null && targetSkin.getSkinId() == -1) {
            throw new IllegalArgumentException("Tried saving preferences without skin");
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);
            if (targetSkin == null) {
                stmt = con.prepareStatement("DELETE FROM " + PREFERENCES_TABLE + " WHERE UUID=?");
                stmt.setString(1, preferences.getUuid().toString().replace("-", ""));
                stmt.executeUpdate();
            } else {
                stmt = con.prepareStatement("REPLACE INTO " + PREFERENCES_TABLE + " (UUID, TargetSkin) VALUES (?, ?)");
                stmt.setString(1, preferences.getUuid().toString().replace("-", ""));
                stmt.setInt(2, targetSkin.getSkinId());
                stmt.executeUpdate();
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save preferences", sqlEx);
        } finally {
            closeQuietly(stmt);
            closeQuietly(con);
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

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);

            stmt = con.prepareStatement("INSERT INTO " + DATA_TABLE
                    + " (Timestamp, UUID, Name, SlimModel, SkinURL, CapeURL, Signature) VALUES"
                    + " (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, skinData.getTimestamp());
            stmt.setString(2, skinData.getUuid().toString().replace("-", ""));
            stmt.setString(3, skinData.getName());
            stmt.setBoolean(4, skinData.isSlimModel());
            stmt.setString(5, skinUrl);
            stmt.setString(6, skinData.getCapeURL());
            stmt.setBytes(7, BaseEncoding.base64().decode(skinData.getEncodedSignature()));

            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                skinData.setSkinId(generatedKeys.getInt(1));
                return true;
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query skin data", sqlEx);
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(stmt);
            closeQuietly(con);
        }

        return false;
    }

    private SkinData parseSkinData(ResultSet resultSet) throws SQLException {
        int skinId = resultSet.getInt(1);
        long timestamp = resultSet.getLong(2);
        UUID uuid = ChangeSkinCore.parseId(resultSet.getString(3));
        String name = resultSet.getString(4);

        boolean slimModel = resultSet.getBoolean(5);

        String skinUrl = resultSet.getString(6);
        String capeUrl = resultSet.getString(7);

        byte[] signature = resultSet.getBytes(8);
        return new SkinData(skinId, timestamp, uuid, name, slimModel, skinUrl, capeUrl, signature);
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception sqlEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close connection", sqlEx);
            }
        }
    }
}
