package com.github.games647.changeskin.sponge.commands;

import org.spongepowered.api.command.spec.CommandSpec;

@FunctionalInterface
public interface ChangeSkinCommand {

    CommandSpec buildSpec();
}
