package com.github.games647.changeskin.sponge.commands;

import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.tasks.SkinUploader;

import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class UploadCommand implements CommandExecutor {

    private final ChangeSkinSponge plugin;

    public UploadCommand(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String url = args.<String>getOne("url").get();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            List<Account> accounts = plugin.getCore().getUploadAccounts();
            if (accounts.isEmpty()) {
                plugin.sendMessageKey(src, "no-accounts");
            } else {
                Account uploadAccount = accounts.get(0);
                Runnable skinUploader = new SkinUploader(plugin, src, uploadAccount, url);
                plugin.getGame().getScheduler().createTaskBuilder().async().execute(skinUploader).submit(plugin);
            }
        } else {
            plugin.sendMessageKey(src, "no-valid-url");
        }

        return CommandResult.success();
    }
}
