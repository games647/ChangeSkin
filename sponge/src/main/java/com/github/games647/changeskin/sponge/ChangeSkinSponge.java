package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.sponge.commands.SelectCommand;
import com.github.games647.changeskin.sponge.commands.SetCommand;
import com.github.games647.changeskin.sponge.commands.InvalidateCommand;
import com.github.games647.changeskin.sponge.commands.UploadCommand;
import com.google.inject.Inject;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class ChangeSkinSponge implements PlatformPlugin<MessageReceiver> {

    @Inject
    @DefaultConfig(sharedRoot = false)
    //We will place more than one config there (i.e. H2/SQLite database)
    private Path defaultConfigFile;

    private final Logger logger;
    private final Game game;
    private final PluginContainer pluginContainer;

    private ChangeSkinCore core;

    private RawDataChannel pluginChannel;

    @Inject
    public ChangeSkinSponge(org.slf4j.Logger logger, Game game, PluginContainer pluginContainer) {
        this.logger = new SLF4JBridgeLogger(logger);
        this.game = game;
        this.pluginContainer = pluginContainer;
    }

    @Listener //load config and database
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        core = new ChangeSkinCore(this);
        try {
            core.load();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error loading config. Disabling plugin...", ex);
            return;
        }
    }

    @Listener //command and event register
    public void onInit(GameInitializationEvent initEvent) {
        CommandManager commandManager = game.getCommandManager();
        commandManager.register(this, CommandSpec.builder()
                .executor(new SelectCommand(this))
                .arguments(GenericArguments.string(Text.of("skinName")))
                .build(), "skin-select");

        commandManager.register(this, CommandSpec.builder()
                .executor(new UploadCommand(this))
                .arguments(GenericArguments.string(Text.of("url")))
                .build(), "skin-upload");

        commandManager.register(this, CommandSpec.builder()
                .executor(new SetCommand(this))
                .arguments(
                        GenericArguments.string(Text.of("skin")),
                        GenericArguments.flags().flag("keep").buildWith(GenericArguments.none()))
                .build(), "changeskin", "setskin");

        commandManager.register(this, CommandSpec.builder()
                .executor(new InvalidateCommand(this))
                .build(), "skininvalidate", "skin-invalidate");

        game.getEventManager().registerListeners(this, new LoginListener(this));
        pluginChannel = game.getChannelRegistrar().createRawChannel(this, pluginContainer.getId());
        pluginChannel.addListener(new BungeeListener(this));
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    public boolean checkPermission(CommandSource invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(pluginContainer.getId().toLowerCase() + ".skin.whitelist." + uuid.toString())) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessageKey(invoker, "no-permission");
        }

        return false;
    }

    public void sendMessageKey(CommandSource sender, String key) {
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

    public void sendMessageKey(CommandSource sender, String key, Object... arguments) {
        if (core == null) {
            return;
        }

        String message = core.getMessage(key);
        if (message != null && sender != null) {
            String formatted = MessageFormat.format(message, arguments);
            sender.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(formatted));
        }
    }

    public void cacheSponge(SkinData skin) {
        //cache the request for Sponge
        GameProfileCache profileCache = game.getServer().getGameProfileManager().getCache();

        GameProfile gameProfile = GameProfile.of(skin.getUuid(), skin.getName());
        profileCache.add(gameProfile);

        ProfileProperty skinProperty = ProfileProperty
                .of(ChangeSkinCore.SKIN_KEY, skin.getEncodedData(), skin.getEncodedSignature());
        gameProfile.getPropertyMap().put(ChangeSkinCore.SKIN_KEY, skinProperty);
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public RawDataChannel getPluginChannel() {
        return pluginChannel;
    }

    public Game getGame() {
        return game;
    }

    @Override
    public String getName() {
        return PomData.NAME;
    }

    @Override
    public File getDataFolder() {
        return defaultConfigFile.toFile().getParentFile();
    }

    public java.util.logging.Logger getLogger() {
        return logger;
    }

    @Override
    public void sendMessage(MessageReceiver receiver, String message) {
        receiver.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }
}
