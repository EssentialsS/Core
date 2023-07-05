package org.essentialss.implementation.messages.adapter.player.command.mute;

import net.kyori.adventure.text.Component;
import org.essentialss.implementation.EssentialsSMain;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.message.MuteType;
import org.essentialss.api.message.adapters.player.command.mute.UnmutedMessageAdapter;
import org.essentialss.api.message.placeholder.SPlaceHolder;
import org.essentialss.api.message.placeholder.SPlaceHolders;
import org.essentialss.api.message.placeholder.wrapper.collection.AndCollectionWrapperPlaceholder;
import org.essentialss.api.player.data.SGeneralPlayerData;
import org.essentialss.api.player.data.SGeneralUnloadedData;
import org.essentialss.implementation.config.value.modifiers.SingleDefaultConfigValueWrapper;
import org.essentialss.implementation.config.value.simple.ComponentConfigValue;
import org.essentialss.implementation.messages.adapter.AbstractMessageAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("i-am-message-adapter")
public class UnmutedMessageAdapterImpl extends AbstractMessageAdapter implements UnmutedMessageAdapter {

    private static final SingleDefaultConfigValueWrapper<Component> CONFIG_VALUE;

    static {
        ComponentConfigValue messageConfigValue = new ComponentConfigValue("player", "command", "mute", "Unmuted");
        Component defaultValue = Component.text("You unmuted " + SPlaceHolders.PLAYER_NAME.formattedPlaceholderTag());
        CONFIG_VALUE = new SingleDefaultConfigValueWrapper<>(messageConfigValue, defaultValue);
    }

    public UnmutedMessageAdapterImpl() {
        super(CONFIG_VALUE);
    }

    @Override
    public @NotNull Component adaptMessage(@NotNull Component message, @NotNull SGeneralUnloadedData playerData, @NotNull MuteType... types) {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();

        message = messageManager.adaptMessageFor(message, playerData);
        if (playerData instanceof SGeneralPlayerData) {
            Player spongePlayer = ((SGeneralPlayerData) playerData).spongePlayer();
            message = messageManager.adaptMessageFor(message, spongePlayer);
        }

        Optional<GameProfile> opProfile = playerData.profile();
        if (opProfile.isPresent()) {
            message = messageManager.adaptMessageFor(message, opProfile.get());
        }

        List<? extends AndCollectionWrapperPlaceholder<MuteType>> muteTypePlaceholders = messageManager
                .mappedPlaceholdersFor(MuteType.class)
                .stream()
                .map(AndCollectionWrapperPlaceholder::new)
                .collect(Collectors.toList());
        for (AndCollectionWrapperPlaceholder<MuteType> placeholder : muteTypePlaceholders) {
            message = placeholder.apply(message, Arrays.asList(types));
        }

        return message;
    }

    @Override
    public @NotNull SingleConfigValue.Default<Component> configValue() {
        return CONFIG_VALUE;
    }

    @Override
    public @NotNull Collection<SPlaceHolder<?>> supportedPlaceholders() {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();

        Collection<SPlaceHolder<?>> placeholders = new LinkedList<>();
        placeholders.addAll(messageManager.placeholdersFor(SGeneralUnloadedData.class));
        placeholders.addAll(messageManager.placeholdersFor(GameProfile.class));
        placeholders.addAll(messageManager
                                    .placeholdersFor(MuteType.class)
                                    .stream()
                                    .map(placeholder -> (SPlaceHolder<?>) new AndCollectionWrapperPlaceholder<>(placeholder))
                                    .collect(Collectors.toList()));
        return placeholders;
    }
}
