package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.PlatformPlugin;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.SkinProperty;
import com.github.games647.changeskin.sponge.commands.InvalidateCommand;
import com.github.games647.changeskin.sponge.commands.SelectCommand;
import com.github.games647.changeskin.sponge.commands.SetCommand;
import com.github.games647.changeskin.sponge.commands.UploadCommand;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.serializer.TextSerializers;

@Singleton
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION,
        url = PomData.URL, description = PomData.DESCRIPTION)
public class ChangeSkinSponge implements PlatformPlugin<CommandSource> {

    private final Path dataFolder;
    private final Logger logger;
    private final Injector injector;

    private final ChangeSkinCore core = new ChangeSkinCore(this);

    private boolean initialized;

    //We will place more than one config there (i.e. H2/SQLite database) -> sharedRoot = false
    @Inject
    ChangeSkinSponge(Logger logger, @ConfigDir(sharedRoot = false) Path dataFolder, Injector injector) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.injector = injector.createChildInjector(binder -> binder.bind(ChangeSkinCore.class).toInstance(core));
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        //load config and database
        try {
            core.load(true);
            initialized = true;
        } catch (Exception ex) {
            logger.error("Error initializing plugin. Disabling...", ex);
        }
    }

    @Listener
    public void onInit(GameInitializationEvent initEvent) {
        if (!initialized)
            return;

        CommandManager cmdManager = Sponge.getCommandManager();

        //command and event register
        cmdManager.register(this, injector.getInstance(SelectCommand.class).buildSpec(), "skin-select", "skinselect");
        cmdManager.register(this, injector.getInstance(UploadCommand.class).buildSpec(), "skin-upload");
        cmdManager.register(this, injector.getInstance(SetCommand.class).buildSpec(), "changeskin", "setskin", "skin");
        cmdManager.register(this, injector.getInstance(InvalidateCommand.class)
                .buildSpec(), "skininvalidate", "skin-invalidate");

        Sponge.getEventManager().registerListeners(this, injector.getInstance(LoginListener.class));
        RawDataChannel pluginChannel = Sponge.getChannelRegistrar().createRawChannel(this, PomData.ARTIFACT_ID);
        pluginChannel.addListener(injector.getInstance(BungeeListener.class));
    }

    @Listener
    public void onShutdown(GameStoppingServerEvent stoppingServerEvent) {
        core.close();
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    @Override
    public boolean checkWhitelistPermission(CommandSource invoker, UUID uuid, boolean sendMessage) {
        if (invoker.hasPermission(PomData.ARTIFACT_ID + ".skin.whitelist." + uuid)) {
            return true;
        }

        //disallow - not whitelisted or blacklisted
        if (sendMessage) {
            sendMessage(invoker, "no-permission");
        }

        return false;
    }

    public void applySkin(GameProfile profile, SkinModel targetSkin) {
        //remove existing skins
        profile.getPropertyMap().clear();

        if (targetSkin != null) {
            GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
            ProfileProperty profileProperty = profileManager.createProfileProperty(SkinProperty.SKIN_KEY
                    , targetSkin.getEncodedValue(), targetSkin.getSignature());
            profile.getPropertyMap().put(SkinProperty.SKIN_KEY, profileProperty);
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
    public void sendMessage(CommandSource receiver, String key) {
        String message = core.getMessage(key);
        if (message != null && receiver != null) {
            receiver.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
        }
    }
}
