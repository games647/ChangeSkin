package com.github.games647.changeskin.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.io.BaseEncoding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SkinStorage {

    private static final String PREFERENCES_TABLE = "preferences";
    private static final String DATA_TABLE = "skinData";

    private final String driver;
    private final String jdbcUrl;
    private final String username;
    private final String pass;

    private final ChangeSkinCore plugin;

    //this is thread-safe in order to save and load from different threads like the skin download
    private final ConcurrentMap<UUID, UserPreferences> preferencesCache = buildCache();
    private final ConcurrentMap<Integer, SkinData> skinCache = buildCache();
    private final ConcurrentMap<UUID, SkinData> skinUUIDCache = buildCache();

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
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);
            Statement statement = con.createStatement();
            String createDataStmt = "CREATE TABLE IF NOT EXISTS " + DATA_TABLE + " ("
                    + "`SkinID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "`Timestamp` BIGINT NOT NULL, "
                    + "`UUID` CHAR(36) NOT NULL, "
                    + "`Name` VARCHAR(16) NOT NULL, "
                    + "`SlimModel` BIT DEFAULT 0 NOT NULL, "
                    + "`SkinURL` VARCHAR(255) NOT NULL, "
                    + "`CapeURL` VARCHAR(255), "
                    + "`Signature` BLOB NOT NULL"
                    //SQLite doesn't support this on a create table statement
                    //+ "INDEX(`UUID`)"
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
                createDataStmt = createDataStmt.replace("AUTO_INCREMENT", "AUTOINCREMENT");
            }

            statement.executeUpdate(createDataStmt);
            statement.executeUpdate(createPreferencesStmt);
        } finally {
            closeQuietly(con);
        }
    }

    public ConcurrentMap<Integer, SkinData> getSkinCache() {
        return skinCache;
    }

    public ConcurrentMap<UUID, SkinData> getSkinUUIDCache() {
        return skinUUIDCache;
    }

    public UserPreferences getPreferences(UUID uuid, boolean fetch) {
        if (preferencesCache.containsKey(uuid)) {
            return preferencesCache.get(uuid);
        } else if (fetch) {
            Connection con = null;
            try {
                con = DriverManager.getConnection(jdbcUrl, username, pass);

                PreparedStatement statement = con.prepareStatement("SELECT TargetSkin FROM " + PREFERENCES_TABLE
                        + " WHERE UUID=? LIMIT 1");

                statement.setString(1, uuid.toString().replace("-", ""));
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int targetSkinId = resultSet.getInt(1);
                    SkinData skinData = getSkin(targetSkinId, true);
                    UserPreferences userPreferences = new UserPreferences(uuid, skinData);
                    preferencesCache.put(uuid, userPreferences);
                    return new UserPreferences(uuid, skinData);
                } else {
                    UserPreferences userPreferences = new UserPreferences(uuid);
                    preferencesCache.put(uuid, userPreferences);
                    return userPreferences;
                }
            } catch (SQLException sqlEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to query preferences", sqlEx);
            } finally {
                closeQuietly(con);
            }
        }

        return null;
    }

    public SkinData getSkin(int targetSkinId, boolean fetch) {
        if (skinCache.containsKey(targetSkinId)) {
            return skinCache.get(targetSkinId);
        } else if (fetch) {
            Connection con = null;
            try {
                con = DriverManager.getConnection(jdbcUrl, username, pass);

                PreparedStatement statement = con.prepareStatement("SELECT * FROM " + DATA_TABLE
                        + " WHERE SkinID=? LIMIT 1");

                statement.setInt(1, targetSkinId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    long timestamp = resultSet.getLong(2);
                    UUID uuid = ChangeSkinCore.parseId(resultSet.getString(3));
                    String name = resultSet.getString(4);

                    boolean slimModel = resultSet.getBoolean(5);

                    String skinUrl = resultSet.getString(6);
                    String capeUrl = resultSet.getString(7);

                    String signature = BaseEncoding.base64().encode(resultSet.getBytes(8));
                    SkinData skinData = new SkinData(targetSkinId, timestamp, uuid, name, slimModel
                            , skinUrl, capeUrl, signature);
                    skinCache.put(targetSkinId, skinData);
                    skinUUIDCache.put(uuid, skinData);
                    return skinData;
                }
            } catch (SQLException sqlEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to query skin data", sqlEx);
            } finally {
                closeQuietly(con);
            }
        }

        return null;
    }

    public SkinData getSkin(UUID skinUUID, boolean fetch) {
        if (skinUUIDCache.containsKey(skinUUID)) {
            return skinUUIDCache.get(skinUUID);
        } else if (fetch) {
            Connection con = null;
            try {
                con = DriverManager.getConnection(jdbcUrl, username, pass);

                PreparedStatement statement = con.prepareStatement("SELECT * FROM " + DATA_TABLE
                        + " WHERE UUID=? LIMIT 1");

                statement.setString(1, skinUUID.toString().replace("-", ""));
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int skinId = resultSet.getInt(1);
                    long timestamp = resultSet.getLong(2);
                    UUID uuid = ChangeSkinCore.parseId(resultSet.getString(3));
                    String name = resultSet.getString(4);

                    boolean slimModel = resultSet.getBoolean(5);

                    String skinUrl = resultSet.getString(6);
                    String capeUrl = resultSet.getString(7);

                    String signature = BaseEncoding.base64().encode(resultSet.getBytes(8));
                    SkinData skinData = new SkinData(skinId, timestamp, uuid, name, slimModel
                            , skinUrl, capeUrl, signature);
                    skinCache.put(skinId, skinData);
                    skinUUIDCache.put(uuid, skinData);
                    return skinData;
                }
            } catch (SQLException sqlEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to query skin data", sqlEx);
            } finally {
                closeQuietly(con);
            }
        }

        return null;
    }

    public void save(UserPreferences preferences) {
        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin != null && targetSkin.getSkinId() == -1) {
            plugin.getLogger().warning("Tried saving preferences without target skin. "
                    + "Please report this to the author");
            return;
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);
            if (targetSkin == null) {
                PreparedStatement statement = con.prepareStatement("DELETE FROM " + PREFERENCES_TABLE
                        + " WHERE UUID=?");
                statement.setString(1, preferences.getUuid().toString().replace("-", ""));
                statement.executeUpdate();
            } else {
                PreparedStatement statement = con.prepareStatement("REPLACE INTO " + PREFERENCES_TABLE
                        + " (UUID, TargetSkin) VALUES (?, ?)");
                statement.setString(1, preferences.getUuid().toString().replace("-", ""));
                statement.setInt(2, targetSkin.getSkinId());

                statement.executeUpdate();
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save preferences", sqlEx);
        } finally {
            closeQuietly(con);
        }
    }

    public boolean save(SkinData skinData) {
        if (skinData == null || skinData.getSkinURL() == null) {
            //invalid skin
            return false;
        }

        if (skinData.getSkinId() != -1) {
            //skin already set
            return true;
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, pass);

            PreparedStatement statement = con.prepareStatement("INSERT INTO " + DATA_TABLE
                    + " (Timestamp, UUID, Name, SlimModel, SkinURL, CapeURL, Signature) VALUES"
                    + " (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            statement.setLong(1, skinData.getTimestamp());
            statement.setString(2, skinData.getUuid().toString().replace("-", ""));
            statement.setString(3, skinData.getName());
            statement.setBoolean(4, skinData.isSlimModel());
            statement.setString(5, skinData.getSkinURL());
            statement.setString(6, skinData.getCapeURL());
            statement.setBytes(7, BaseEncoding.base64().decode(skinData.getEncodedSignature()));

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                skinData.setSkinId(generatedKeys.getInt(1));
                return true;
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query skin data", sqlEx);
        } finally {
            closeQuietly(con);
        }

        return false;
    }

    public void close() {
        skinCache.clear();
        skinUUIDCache.clear();
        preferencesCache.clear();
    }

    private void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException sqlEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close connection", sqlEx);
            }
        }
    }

    private <K, V> ConcurrentMap<K, V> buildCache() {
        return CacheBuilder
                .newBuilder()
                .maximumSize(1024 * 5)
                .expireAfterAccess(3, TimeUnit.HOURS)
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        //A key should be inserted manually
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }).asMap();
    }
}
