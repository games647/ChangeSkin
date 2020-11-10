package com.github.games647.changeskin.bungee.command;

import net.md_5.bungee.api.plugin.Command;

public abstract class ChangeSkinCommand extends Command {

    public ChangeSkinCommand(String name) {
        super(name);
    }

    public ChangeSkinCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }
}
