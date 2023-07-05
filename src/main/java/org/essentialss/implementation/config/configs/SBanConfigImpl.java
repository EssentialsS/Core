package org.essentialss.implementation.config.configs;

import net.kyori.adventure.text.Component;
import org.essentialss.implementation.EssentialsSMain;
import org.essentialss.api.ban.BanMultiplayerScreenOptions;
import org.essentialss.api.config.configs.BanConfig;
import org.essentialss.api.config.value.ConfigValue;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.implementation.config.value.modifiers.SingleDefaultConfigValueWrapper;
import org.essentialss.implementation.config.value.primitive.BooleanConfigValue;
import org.essentialss.implementation.config.value.simple.ComponentConfigValue;
import org.essentialss.implementation.config.value.simple.EnumConfigValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class SBanConfigImpl implements BanConfig {

    private static final SingleConfigValue.Default<BanMultiplayerScreenOptions> SHOW_BAN_ON_MULTIPLAYER_SCREEN;
    private static final SingleConfigValue.Default<Boolean> SHOW_FULL_ON_MULTIPLAYER_SCREEN;
    private static final SingleConfigValue.Default<Component> BAN_MESSAGE;
    private static final ComponentConfigValue TEMP_BAN_MESSAGE;
    private static final SingleConfigValue.Default<Boolean> USE_BAN_MESSAGE_FOR_TEMP_BAN;
    private static final SingleConfigValue.Default<Boolean> USE_ESSENTIALS_S_BAN_COMMANDS;

    static {
        SHOW_BAN_ON_MULTIPLAYER_SCREEN = new SingleDefaultConfigValueWrapper<>(
                new EnumConfigValue<>(BanMultiplayerScreenOptions.class, "messages", "ShowOnMultiplayerScreen"), BanMultiplayerScreenOptions.DEFAULT);
        SHOW_FULL_ON_MULTIPLAYER_SCREEN = new BooleanConfigValue("messages", "ShowFullOnMultiplayerScreen");
        BAN_MESSAGE = new SingleDefaultConfigValueWrapper<>(new ComponentConfigValue("messages", "BannedMessage"),
                                                            Component.text("You are banned from this server"));
        TEMP_BAN_MESSAGE = new ComponentConfigValue("messages", "TempBannedMessage");
        USE_BAN_MESSAGE_FOR_TEMP_BAN = new BooleanConfigValue(true, "messages", "UseBanMessageForTempBan");
        USE_ESSENTIALS_S_BAN_COMMANDS = new BooleanConfigValue(true, "enabled", "banCommands", "EssentialsSBanCommands");

    }

    @Override
    public SingleConfigValue.Default<Component> banMessage() {
        return BAN_MESSAGE;
    }

    @Override
    public SingleConfigValue.Default<BanMultiplayerScreenOptions> showBanOnMultiplayerScreen() {
        return SHOW_BAN_ON_MULTIPLAYER_SCREEN;
    }

    @Override
    public SingleConfigValue.Default<Boolean> showFullOnMultiplayerScreen() {
        return SHOW_FULL_ON_MULTIPLAYER_SCREEN;
    }

    @Override
    public SingleConfigValue<Component> tempBanMessage() {
        return TEMP_BAN_MESSAGE;
    }

    @Override
    public SingleConfigValue.Default<Boolean> useBanMessageForTempBan() {
        return USE_BAN_MESSAGE_FOR_TEMP_BAN;
    }

    @Override
    public SingleConfigValue.Default<Boolean> useEssentialsSBanCommands() {
        return USE_ESSENTIALS_S_BAN_COMMANDS;
    }

    @Override
    @SuppressWarnings("ReturnOfNull")
    public @NotNull Collection<ConfigValue<?>> expectedNodes() {
        return Arrays
                .stream(SBanConfigImpl.class.getDeclaredFields())
                .filter(field -> Modifier.isPrivate(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> ConfigValue.class.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (ConfigValue<?>) field.get(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull File file() {
        Path path = Sponge.configManager().pluginConfig(EssentialsSMain.plugin().container()).directory();
        return new File(path.toFile(), "config/BanConfig.conf");
    }

    @Override
    public void update() throws SerializationException {
        ConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoader();
        ConfigurationNode root = loader.createNode();

        SHOW_FULL_ON_MULTIPLAYER_SCREEN.setDefaultIfNotPresent(root);
        SHOW_BAN_ON_MULTIPLAYER_SCREEN.setDefaultIfNotPresent(root);
        BAN_MESSAGE.setDefaultIfNotPresent(root);
        if (null == TEMP_BAN_MESSAGE.parse(root)) {
            TEMP_BAN_MESSAGE.set(root, Component.text("You have been temporary banned"));
        }
        USE_BAN_MESSAGE_FOR_TEMP_BAN.setDefaultIfNotPresent(root);
        USE_ESSENTIALS_S_BAN_COMMANDS.setDefaultIfNotPresent(root);

        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
