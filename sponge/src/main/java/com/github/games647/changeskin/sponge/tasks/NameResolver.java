package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.shared.SharedNameResolver;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class NameResolver extends SharedNameResolver {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;
    private final boolean keepSkin;

    public NameResolver(ChangeSkinSponge plugin, CommandSource invoker, String targetName, Player receiver
            , boolean keepSkin) {
        super(plugin.getCore(), targetName, keepSkin);

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.keepSkin = keepSkin;
    }

    @Override
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }

    @Override
    protected boolean hasSkinPermission(UUID uuid) {
        //todo check skin permissions
        return true;
    }

    @Override
    protected void scheduleDownloader(UUID uuid) {
        //run this is the same thread
        new SkinDownloader(plugin, invoker, receiver, uuid, keepSkin).run();
    }
}
