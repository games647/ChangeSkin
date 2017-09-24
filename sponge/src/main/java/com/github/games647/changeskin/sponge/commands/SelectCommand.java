package com.github.games647.changeskin.sponge.commands;

import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.tasks.SkinSelector;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class SelectCommand implements CommandExecutor {

    private final ChangeSkinSponge plugin;

    public SelectCommand(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            plugin.sendMessageKey(src, "no-console");
            return CommandResult.empty();
        }

        String skinName = args.<String>getOne("skinName").get().toLowerCase().replace("skin-", "");

        try {
            int targetId = Integer.parseInt(skinName);
            Player receiver = (Player) src;
            plugin.getGame().getScheduler().createTaskBuilder().async()
                    .execute(new SkinSelector(plugin, receiver, targetId)).submit(plugin);
        } catch (NumberFormatException numberFormatException) {
            plugin.sendMessageKey(src, "invalid-skin-name");
        }

        return CommandResult.success();
    }
}
