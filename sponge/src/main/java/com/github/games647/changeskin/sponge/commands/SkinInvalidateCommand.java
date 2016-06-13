package com.github.games647.changeskin.sponge.commands;

import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class SkinInvalidateCommand implements CommandExecutor {

    private final ChangeSkinSponge plugin;

    public SkinInvalidateCommand(ChangeSkinSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
