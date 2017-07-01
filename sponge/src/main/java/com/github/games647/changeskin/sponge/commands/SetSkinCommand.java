package com.github.games647.changeskin.sponge.commands;

import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.tasks.NameResolver;
import com.github.games647.changeskin.sponge.tasks.SkinDownloader;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class SetSkinCommand implements CommandExecutor {

    private final ChangeSkinSponge plugin;

    public SetSkinCommand(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            plugin.sendMessage(src, "no-console");
            return CommandResult.empty();
        }

        if (plugin.getCore().isCooldown(((Player) src).getUniqueId())) {
            plugin.sendMessage(src, "cooldown");
            return CommandResult.empty();
        }

        Player receiver = (Player) src;
        String targetSkin = args.<String>getOne("skin").get();
        Optional<String> keepOptional = args.<String>getOne("keep");
        boolean keepSkin = false;
        if (keepOptional.isPresent() && "keep".equalsIgnoreCase(keepOptional.get())) {
            keepSkin = true;
        }

        if (targetSkin.equals("reset")) {
            targetSkin = receiver.getUniqueId().toString();
        }

        if (targetSkin.length() > 16) {
            UUID targetUUID = UUID.fromString(targetSkin);

            if (plugin.getRootNode().getNode("skinPermission").getBoolean()
                    && !plugin.checkPermission(src, targetUUID, true)) {
                return CommandResult.empty();
            }

            plugin.sendMessage(src, "skin-change-queue");
            SkinDownloader skinDownloader = new SkinDownloader(plugin, src, receiver, targetUUID, keepSkin);
            plugin.getGame().getScheduler().createTaskBuilder().async().execute(skinDownloader).submit(plugin);
            return CommandResult.success();
        }

        plugin.sendMessage(src, "queue-name-resolve");
        NameResolver nameResolver = new NameResolver(plugin, src, targetSkin, receiver, keepSkin);
        plugin.getGame().getScheduler().createTaskBuilder().async().execute(nameResolver).submit(plugin);
        return CommandResult.success();
    }
}
