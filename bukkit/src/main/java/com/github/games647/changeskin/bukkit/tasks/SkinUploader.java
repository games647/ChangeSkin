package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.UUID;

import org.bukkit.command.CommandSender;

public class SkinUploader implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;

    private final Account owner;
    private final String url;

    private final String saveName;

    public SkinUploader(ChangeSkinBukkit plugin, CommandSender invoker, Account owner, String url, String name) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.owner = owner;
        this.url = url;
        this.saveName = name;
    }

    public SkinUploader(ChangeSkinBukkit plugin, CommandSender invoker, Account owner, String url) {
        this(plugin, invoker, owner, url, null);
    }

    @Override
    public void run() {
        PlayerProfile profile = owner.getProfile();
        String oldSkinUrl = plugin.getCore().getMojangAuthApi().getSkinUrl(profile.getName());

        UUID uuid = ChangeSkinCore.parseId(profile.getId());
        UUID accessToken = ChangeSkinCore.parseId(owner.getAccessToken());
        plugin.getCore().getMojangAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        SkinData newSkin = plugin.getCore().getMojangSkinApi().downloadSkin(uuid);
        plugin.getStorage().save(newSkin);

        plugin.getCore().getMojangAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
        plugin.sendMessage(invoker, "skin-uploaded", owner.getProfile().getName(), "Skin-" + newSkin.getSkinId());
    }
}
