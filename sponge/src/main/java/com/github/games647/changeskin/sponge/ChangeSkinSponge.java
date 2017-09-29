package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.sponge.commands.InvalidateCommand;
import com.github.games647.changeskin.sponge.commands.SelectCommand;
import com.github.games647.changeskin.sponge.commands.SetCommand;
import com.github.games647.changeskin.sponge.commands.UploadCommand;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class ChangeSkinSponge implements PlatformPlugin<MessageReceiver> {

    private final Path dataFolder;
    private final Logger logger;

    private ChangeSkinCore core;

    //We will place more than one config there (i.e. H2/SQLite database) -> sharedRoot = false
    @Inject
    public ChangeSkinSponge(Logger logger, @ConfigDir(sharedRoot = false) Path dataFolder) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    @Listener //load config and database
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        core = new ChangeSkinCore(this);
        try {
            core.load();
        } catch (Exception ex) {
            logger.error("Error loading config. Disabling plugin...", ex);
        }
    }

    @Listener //command and event register
    public void onInit(GameInitializationEvent initEvent) {
        CommandManager commandManager = Sponge.getCommandManager();
        commandManager.register(this, CommandSpec.builder()
                .executor(new SelectCommand(this))
                .arguments(string(of("skinName")))
                .build(), "skin-select");

        commandManager.register(this, CommandSpec.builder()
                .executor(new UploadCommand(this))
                .arguments(string(of("url")))
                .build(), "skin-upload");

        commandManager.register(this, CommandSpec.builder()
                .executor(new SetCommand(this))
                .arguments(
                        string(of("skin")),
                        flags().flag("keep").buildWith(GenericArguments.none()))
                .build(), "changeskin", "setskin");

        commandManager.register(this, CommandSpec.builder()
                .executor(new InvalidateCommand(this))
                .build(), "skininvalidate", "skin-invalidate");

        Sponge.getEventManager().registerListeners(this, new LoginListener(this));
        RawDataChannel pluginChannel = Sponge.getChannelRegistrar().createRawChannel(this, PomData.ARTIFACT_ID);
        pluginChannel.addListener(new BungeeListener(this, pluginChannel));
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    public boolean checkPermission(CommandSource invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(PomData.ARTIFACT_ID + ".skin.whitelist." + uuid)) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessageKey(invoker, "no-permission");
        }

        return false;
    }

    public void sendMessageKey(MessageReceiver sender, String key) {
        //todo: this shouldn't be the key - it should be the actual message
        if (core == null) {
            return;
        }

        String message = core.getMessage(key);
        if (message != null && sender != null) {
            sender.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
        }
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return null;
    }

    public void sendMessageKey(MessageReceiver sender, String key, Object... arguments) {
        if (core == null) {
            return;
        }

        String message = core.getMessage(key);
        if (message != null && sender != null) {
            String formatted = MessageFormat.format(message, arguments);
            sender.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(formatted));
        }
    }

    @Override
    public String getName() {
        return PomData.NAME;
    }

    @Override
    public Path getPluginFolder() {
        return dataFolder;
    }

    @Override
    public Logger getLog() {
        return logger;
    }

    @Override
    public void sendMessage(MessageReceiver receiver, String message) {
        receiver.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }
}
