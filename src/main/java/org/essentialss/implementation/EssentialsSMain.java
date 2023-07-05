package org.essentialss.implementation;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.essentialss.api.EssentialsSAPI;
import org.essentialss.api.ban.SBanManager;
import org.essentialss.api.config.SConfigManager;
import org.essentialss.api.config.configs.BanConfig;
import org.essentialss.api.config.configs.GeneralConfig;
import org.essentialss.api.group.GroupManager;
import org.essentialss.api.kit.KitManager;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.player.SPlayerManager;
import org.essentialss.api.utils.Singleton;
import org.essentialss.api.utils.parameter.ParameterAdapter;
import org.essentialss.api.world.SWorldManager;
import org.essentialss.implementation.ban.SBanManagerImpl;
import org.essentialss.implementation.command.ParameterAdapters;
import org.essentialss.implementation.command.back.BackCommand;
import org.essentialss.implementation.command.back.BackListCommand;
import org.essentialss.implementation.command.back.ForwardCommand;
import org.essentialss.implementation.command.back.ForwardListCommand;
import org.essentialss.implementation.command.ban.BanCommands;
import org.essentialss.implementation.command.essentialss.EssentialsSCommand;
import org.essentialss.implementation.command.essentialss.plugins.PluginsCommand;
import org.essentialss.implementation.command.feed.FeedCommand;
import org.essentialss.implementation.command.flame.GlowCommand;
import org.essentialss.implementation.command.gamemode.GamemodeCommand;
import org.essentialss.implementation.command.god.GodModeCommand;
import org.essentialss.implementation.command.hat.HatCommand;
import org.essentialss.implementation.command.inventory.DisplayInventoryCommand;
import org.essentialss.implementation.command.invsee.InventorySeeCommand;
import org.essentialss.implementation.command.kit.KitCommand;
import org.essentialss.implementation.command.mute.MuteCommand;
import org.essentialss.implementation.command.mute.UnmuteCommand;
import org.essentialss.implementation.command.nick.NicknameCommand;
import org.essentialss.implementation.command.nick.WhoIsCommand;
import org.essentialss.implementation.command.ping.PingCommand;
import org.essentialss.implementation.command.point.PointCommand;
import org.essentialss.implementation.command.point.list.ListSpawnCommand;
import org.essentialss.implementation.command.point.list.ListWarpCommand;
import org.essentialss.implementation.command.run.RunCommand;
import org.essentialss.implementation.command.spy.CommandSpyCommand;
import org.essentialss.implementation.command.teleport.RandomTeleportCommand;
import org.essentialss.implementation.command.teleport.request.*;
import org.essentialss.implementation.command.unban.UnbanCommands;
import org.essentialss.implementation.command.vanish.UnvanishCommand;
import org.essentialss.implementation.command.vanish.VanishCommand;
import org.essentialss.implementation.command.world.ListWorldsCommand;
import org.essentialss.implementation.command.world.WorldCommand;
import org.essentialss.implementation.config.SConfigManagerImpl;
import org.essentialss.implementation.group.GroupManagerImpl;
import org.essentialss.implementation.kit.KitManagerImpl;
import org.essentialss.implementation.listeners.afk.AwayFromKeyboardListeners;
import org.essentialss.implementation.listeners.ban.BanConnectionListeners;
import org.essentialss.implementation.listeners.chat.ChatListener;
import org.essentialss.implementation.listeners.chat.MuteListener;
import org.essentialss.implementation.listeners.chat.SpyListener;
import org.essentialss.implementation.listeners.chat.VanillaMessageListener;
import org.essentialss.implementation.listeners.connection.ConnectionListeners;
import org.essentialss.implementation.listeners.data.DataListeners;
import org.essentialss.implementation.listeners.data.GodListeners;
import org.essentialss.implementation.messages.SMessageManagerImpl;
import org.essentialss.implementation.misc.OrElse;
import org.essentialss.implementation.player.SPlayerManagerImpl;
import org.essentialss.implementation.schedules.AwayFromKeyboardCheckScheduler;
import org.essentialss.implementation.schedules.CooldownScheduler;
import org.essentialss.implementation.schedules.UpdateCheck;
import org.essentialss.implementation.world.SWorldManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;

@Plugin("essentials-s")
public class EssentialsSMain implements EssentialsSAPI {

    private static EssentialsSMain plugin;
    private final PluginContainer container;
    private final Logger logger;
    private final Singleton<SWorldManager> worldManager = new Singleton<>(SWorldManagerImpl::new);
    private final Singleton<SConfigManager> configManager = new Singleton<>(SConfigManagerImpl::new);
    private final Singleton<SPlayerManager> playerManager = new Singleton<>(SPlayerManagerImpl::new);
    private final Singleton<SBanManager> banManager = new Singleton<>(SBanManagerImpl::new);
    private final Singleton<MessageManager> messageManager = new Singleton<>(SMessageManagerImpl::new);
    private final Singleton<KitManager> kitManager = new Singleton<>(KitManagerImpl::new);
    private final Collection<ParameterAdapter> parameterAdapters = new LinkedTransferQueue<>();
    private final Singleton<GroupManager> groupManager = new Singleton<>(GroupManagerImpl::new);

    @SuppressWarnings("AccessStaticViaInstance")
    @Inject
    public EssentialsSMain(PluginContainer container, Logger logger) {
        this.plugin = this;
        this.container = container;
        this.logger = logger;
        this.parameterAdapters.addAll(Arrays.asList(ParameterAdapters.values()));
    }

    @Override
    @NotNull
    public Singleton<SBanManager> banManager() {
        return this.banManager;
    }

    @Override
    @NotNull
    public Singleton<SConfigManager> configManager() {
        return this.configManager;
    }

    @Override
    @NotNull
    public PluginContainer container() {
        return this.container;
    }

    @NotNull
    @Override
    public Singleton<GroupManager> groupManager() {
        return this.groupManager;
    }

    @Override
    @NotNull
    public Singleton<KitManager> kitManager() {
        return this.kitManager;
    }

    @Override
    @NotNull
    public Singleton<MessageManager> messageManager() {
        return this.messageManager;
    }

    @Override
    @NotNull
    public Collection<ParameterAdapter> parameterAdapters() {
        return this.parameterAdapters;
    }

    @Override
    @NotNull
    public Singleton<SPlayerManager> playerManager() {
        return this.playerManager;
    }

    @Override
    @NotNull
    public Singleton<SWorldManager> worldManager() {
        return this.worldManager;
    }

    @NotNull
    public Logger logger() {
        return this.logger;
    }

    @Listener
    public void onCommandRegister(RegisterCommandEvent<Command.Parameterized> event) {
        //misc
        event.register(this.container, RunCommand.createRunCommand(), "run", "execute");
        event.register(this.container, EssentialsSCommand.createEssentialsCommand(), "essentialss");
        event.register(this.container, PingCommand.createPingCommand(), "ping");
        event.register(this.container, PluginsCommand.createPluginsCommand(), "plugins", "pl");
        event.register(this.container, GlowCommand.createGlowCommand(), "glow");
        event.register(this.container, FeedCommand.createFeedCommand(), "feed");
        event.register(this.container, WorldCommand.createWorldCommand(), "world");
        event.register(this.container, ListWorldsCommand.createListCommand(), "worlds");

        //overrides
        event.register(this.container, GamemodeCommand.createGamemodeCommand(), "gamemode", "gm");

        //flame
        /*event.register(this.container, FlameCommand.createFlameCommand(), "flame");
        event.register(this.container, FlameCommand.createFlameOffCommand(), "flameoff");
        event.register(this.container, FlameCommand.createFlameOnCommand(), "flameon");*/

        //kit
        event.register(this.container, KitCommand.createKitCommand(), "kit");

        //spy
        event.register(this.container, CommandSpyCommand.createCommandSpyCommand(), "commandspy", "cspy");

        //inventory
        event.register(this.container, HatCommand.createHatCommand(), "hat");
        event.register(this.container, InventorySeeCommand.createInventorySeeCommand(), "inventorysee", "invsee");
        event.register(this.container, DisplayInventoryCommand.createCraftingInventoryCommand(), "crafting", "workbench");
        event.register(this.container, DisplayInventoryCommand.createBrewInventoryCommand(), "brewing", "brew");
        event.register(this.container, DisplayInventoryCommand.createAnvilInventoryCommand(), "anvil");
        event.register(this.container, DisplayInventoryCommand.createFurnaceInventoryCommand(), "furnace");
        event.register(this.container, DisplayInventoryCommand.createBlastFurnaceInventoryCommand(), "blast");

        //warp
        event.register(this.container, PointCommand.createWarpCommand(), "warp");
        event.register(this.container, ListWarpCommand.createWarpListCommand(), "warps");

        //spawn
        event.register(this.container, PointCommand.createSpawnCommand(), "spawn");
        event.register(this.container, ListSpawnCommand.createSpawnListCommand(), "spawns");

        //nickname
        event.register(this.container, NicknameCommand.createNicknameCommand(), "nickname", "nick");
        event.register(this.container, WhoIsCommand.createWhoIsCommand(), "whois", "realname");

        //mute
        event.register(this.container, MuteCommand.createMuteCommand(), "mute");
        event.register(this.container, UnmuteCommand.createUnmuteCommand(), "unmute");

        //teleport
        event.register(this.container, TeleportRequestsCommand.createTeleportRequestsCommand(), "teleportrequests", "tprequests", "tpr");
        event.register(this.container, TeleportRequestToPlayerCommand.createTeleportRequestToPlayerCommand(), "teleportto", "tpto", "tpt");
        event.register(this.container, TeleportRequestHerePlayerCommand.createTeleportRequestHerePlayerCommand(), "teleporthere", "tphere", "tph");
        event.register(this.container, TeleportAcceptRequestCommand.createTeleportAcceptCommand(), "tpaccept", "tpa");
        event.register(this.container, TeleportDenyRequestCommand.createTeleportDenyCommand(), "tpdeny", "tpd");
        event.register(this.container, RandomTeleportCommand.createRandomTeleportCommand(), "randomteleport", "rtp");

        //back
        event.register(this.container, BackCommand.createBackCommand(), "back");
        event.register(this.container, ForwardCommand.createForwardCommand(), "forward");
        event.register(this.container, BackListCommand.createBackListCommand(), "backlist", "backl");
        event.register(this.container, ForwardListCommand.createForwardListCommand(), "forwardlist", "forwardl");

        //ban
        BanConfig banConfig = this.banManager().get().banConfig().get();
        if (banConfig.useEssentialsSBanCommands().parseDefault(banConfig)) {
            event.register(this.container, BanCommands.createBanCommand(), "ban");
            event.register(this.container, UnbanCommands.createUnbanCommands(), "unban");
        }

        //vanish
        event.register(this.container, VanishCommand.createVanishCommand(), "vanish", "invisible");
        event.register(this.container, UnvanishCommand.createUnvanishCommand(), "unvanish", "visible");

        //god
        event.register(this.container, GodModeCommand.createGodModeCommand(), "god");
        event.register(this.container, GodModeCommand.createDemiGodModeCommand(), "demigod");

    }

    @Listener
    public void onPluginBoot(ConstructPluginEvent event) {
        if (!event.plugin().equals(this.container)) {
            return;
        }
        this.registerEvents();
        this.registerAsyncedSchedulers();
    }

    @Listener
    public void onServerReady(StartedEngineEvent<Server> event) {
        @NotNull GeneralConfig general = this.configManager().get().general().get();
        if (general.checkForUpdatesOnStartup().parseDefault(general)) {
            UpdateCheck.createDelay(Sponge.systemSubject());
        }
    }

    private void registerAsyncedSchedulers() {
        Scheduler scheduler = Sponge.asyncScheduler();
        scheduler.submit(AwayFromKeyboardCheckScheduler.createTask(), "AwayFromKeyboard");
        scheduler.submit(CooldownScheduler.createKitCooldownTask(), "KitCooldown");
    }

    private void registerEvents() {
        EventManager eventManager = Sponge.eventManager();
        eventManager.registerListeners(this.container, new ConnectionListeners());
        eventManager.registerListeners(this.container, new BanConnectionListeners());
        eventManager.registerListeners(this.container, new AwayFromKeyboardListeners());
        eventManager.registerListeners(this.container, new MuteListener());
        eventManager.registerListeners(this.container, new SpyListener());
        eventManager.registerListeners(this.container, new DataListeners());
        eventManager.registerListeners(this.container, new VanillaMessageListener());
        eventManager.registerListeners(this.container, new GodListeners());

        if (OrElse.ifTry(Exception.class, () -> {
            Class.forName("org.spongepowered.api.event.message.PlayerChatEvent");
            return true;
        }, () -> false)) {
            eventManager.registerListeners(this.container, new ChatListener());
        }
    }

    public static EssentialsSMain plugin() {
        return plugin;
    }
}
