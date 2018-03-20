package com.github.games647.changeskin.bukkit;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class ServerVersionTest {

    @Test
    public void testSpigot() throws Exception {
        ServerVersion version = new ServerVersion("org.bukkit.craftbukkit.v1_12_R1");
        assertThat(version.getPackageVersion(), is("v1_12_R1"));
        assertThat(version.getOBCPackage(), is("org.bukkit.craftbukkit.v1_12_R1"));
        assertThat(version.getNMSPackage(), is("net.minecraft.server.v1_12_R1"));
    }

    @Test
    public void testNoVersion() throws Exception {
        ServerVersion version = new ServerVersion("org.bukkit.craftbukkit");
        assertThat(version.getPackageVersion(), is(""));
        assertThat(version.getOBCPackage(), is("org.bukkit.craftbukkit"));
        assertThat(version.getNMSPackage(), is("net.minecraft.server"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotParse() throws Exception {
        new ServerVersion("");
    }
}
