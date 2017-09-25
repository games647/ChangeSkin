package com.github.games647.changeskin.sponge.commands;

import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.tasks.NameResolver;
import com.github.games647.changeskin.sponge.tasks.SkinDownloader;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class SetCommand implements CommandExecutor {

    private final ChangeSkinSponge plugin;

    public SetCommand(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            plugin.sendMessageKey(src, "no-console");
            return CommandResult.empty();
        }

        if (plugin.getCore().isCooldown(((Player) src).getUniqueId())) {
            plugin.sendMessageKey(src, "cooldown");
            return CommandResult.empty();
        }

        Player receiver = (Player) src;
        String targetSkin = args.<String>getOne("skin").get();
        boolean keepSkin = args.hasAny("keep");

        if ("reset".equals(targetSkin)) {
            targetSkin = receiver.getUniqueId().toString();
        }

        if (targetSkin.length() > 16) {
            UUID targetUUID = UUID.fromString(targetSkin);

            if (plugin.getCore().getConfig().getBoolean("skinPermission")
                    && !plugin.checkPermission(src, targetUUID, true)) {
                return CommandResult.empty();
            }

            plugin.sendMessageKey(src, "skin-change-queue");
            Runnable skinDownloader = new SkinDownloader(plugin, src, receiver, targetUUID, keepSkin);
            Sponge.getScheduler().createTaskBuilder().async().execute(skinDownloader).submit(plugin);
            return CommandResult.success();
        }

        plugin.sendMessageKey(src, "queue-name-resolve");
        Runnable nameResolver = new NameResolver(plugin, src, targetSkin, receiver, keepSkin);
        Sponge.getScheduler().createTaskBuilder().async().execute(nameResolver).submit(plugin);
        return CommandResult.success();
    }
}
