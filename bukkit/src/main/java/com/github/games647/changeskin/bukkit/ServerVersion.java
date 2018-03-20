package com.github.games647.changeskin.bukkit;

import java.util.Objects;

import org.bukkit.Server;

/**
 * Extracts the server package version. For every new Minecraft version the internals are in a different
 * package, because of potential breaking changes.
 */
public class ServerVersion {

    private static final String NMS_PREFIX = "net.minecraft.server";
    private static final String OBC_PREFIX = "org.bukkit.craftbukkit";

    /**
     * Represents a regular expression that will match the version string in a package:
     *    org.bukkit.craftbukkit.v1_6_R2      ->      v1_6_R2
     */
    private static final String PACKAGE_VERSION_MATCHER = "(v\\d+_\\d+_R\\d+)";

    private final String packageVersion;

    private final String nmsPackage;
    private final String obcPackage;

    /**
     * Extract from the Bukkit server class.
     *
     * @param serverClass server class from {@link org.bukkit.Bukkit#getServer()}
     */
    public ServerVersion(Class<? extends Server> serverClass) {
        this(serverClass.getPackage().getName());
    }

    /**
     * Extract from the full package path of any Craftbukkit class
     *
     * @param packageName craftbukkit package like org.bukkit.craftbukkit.v1_6_R2
     */
    public ServerVersion(String packageName) {
        if (!packageName.startsWith(OBC_PREFIX)) {
            throw new IllegalStateException("Unknown package prefix " + packageName);
        }

        // remove everything before the version
        String packName = packageName.replace(OBC_PREFIX, "");
        if (packName.isEmpty()) {
            //no version at all
            packageVersion = "";
            nmsPackage = NMS_PREFIX;
            obcPackage = OBC_PREFIX;
            return;
        }

        //remove the dot for package separation
        String version = packName.substring(1);
        if (!version.matches(PACKAGE_VERSION_MATCHER)) {
            //unknown version schema
            throw new IllegalStateException("Unknown package version " + version);
        }

        packageVersion = version;
        nmsPackage = NMS_PREFIX + '.' + version;
        obcPackage = OBC_PREFIX + '.' + version;
    }

    /**
     * @return full NMS package location like net.minecraft.server.v1_6_R2
     */
    public String getNMSPackage() {
        return nmsPackage;
    }

    /**
     * @return full OBC package location like org.bukkit.craftbukkit.v1_6_R2
     */
    public String getOBCPackage() {
        return obcPackage;
    }

    /**
     * @return package version like v1_6R2
     */
    public String getPackageVersion() {
        return packageVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof ServerVersion) {
            ServerVersion that = (ServerVersion) other;
            return Objects.equals(packageVersion, that.packageVersion) &&
                    Objects.equals(nmsPackage, that.nmsPackage) &&
                    Objects.equals(obcPackage, that.obcPackage);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageVersion, nmsPackage, obcPackage);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "packageVersion='" + packageVersion + '\'' +
                ", nmsPackage='" + nmsPackage + '\'' +
                ", obcPackage='" + obcPackage + '\'' +
                '}';
    }
}
