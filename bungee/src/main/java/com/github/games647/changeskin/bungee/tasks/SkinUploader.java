package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;

public class SkinUploader implements Runnable {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;

    private final Account owner;
    private final String url;

    private final String saveName;

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url, String name) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.owner = owner;
        this.url = url;
        this.saveName = name;
    }

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url) {
        this(plugin, invoker, owner, url, null);
    }

    @Override
    public void run() {
        PlayerProfile profile = owner.getProfile();
        String oldSkinUrl = plugin.getCore().getMojangAuthApi().getSkinUrl(profile.getName());

        UUID uuid = ChangeSkinCore.parseId(profile.getId());
        UUID accessToken = ChangeSkinCore.parseId(owner.getAccessToken());
        plugin.getCore().getMojangAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could proparly cause issues for uuid resolving to this database entry
        SkinData newSkin = plugin.getCore().getMojangSkinApi().downloadSkin(uuid);
        plugin.getStorage().save(newSkin);

        plugin.getCore().getMojangAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
        plugin.sendMessage(invoker, "skin-uploaded", owner.getProfile().getName(), "Skin-" + newSkin.getSkinId());
    }
}