package com.github.games647.changeskin.bukkit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServerVersionTest {

    @Test
    public void testSpigot() throws Exception {
        ServerVersion version = new ServerVersion("org.bukkit.craftbukkit.v1_12_R1");
        assertEquals("v1_12_R1", version.getPackageVersion());
        assertEquals("org.bukkit.craftbukkit.v1_12_R1", version.getOBCPackage());
        assertEquals("net.minecraft.server.v1_12_R1", version.getNMSPackage());
    }

    @Test
    public void testNoVersion() throws Exception {
        ServerVersion version = new ServerVersion("org.bukkit.craftbukkit");
        assertEquals("", version.getPackageVersion());
        assertEquals("org.bukkit.craftbukkit", version.getOBCPackage());
        assertEquals("net.minecraft.server", version.getNMSPackage());
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotParse() throws Exception {
        new ServerVersion("");
    }
}
