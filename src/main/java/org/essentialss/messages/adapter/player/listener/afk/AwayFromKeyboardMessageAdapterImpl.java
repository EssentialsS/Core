package org.essentialss.messages.adapter.player.listener.afk;

import net.kyori.adventure.text.Component;
import org.essentialss.EssentialsSMain;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.message.adapters.player.listener.afk.AwayFromKeyboardMessageAdapter;
import org.essentialss.api.message.placeholder.SPlaceHolder;
import org.essentialss.api.message.placeholder.SPlaceHolders;
import org.essentialss.api.player.data.SGeneralPlayerData;
import org.essentialss.api.utils.arrays.impl.SingleUnmodifiableCollection;
import org.essentialss.config.value.modifiers.SingleDefaultConfigValueWrapper;
import org.essentialss.config.value.simple.ComponentConfigValue;
import org.essentialss.messages.adapter.AbstractEnabledMessageAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("i-am-message-adapter")
public class AwayFromKeyboardMessageAdapterImpl extends AbstractEnabledMessageAdapter implements AwayFromKeyboardMessageAdapter {
    private static final SingleDefaultConfigValueWrapper<Component> CONFIG_VALUE;

    static {
        ComponentConfigValue messageConfigValue = new ComponentConfigValue("player", "interaction", "awayFromKeyboard", "Message");
        Component defaultText = Component.text(SPlaceHolders.PLAYER_NICKNAME.formattedPlaceholderTag() + " is away from their keyboard");
        CONFIG_VALUE = new SingleDefaultConfigValueWrapper<>(messageConfigValue, defaultText);
    }

    public AwayFromKeyboardMessageAdapterImpl() {
        super(true, CONFIG_VALUE);
    }

    @Override
    public @NotNull Component adaptMessage(@NotNull Component messageToAdapt, @NotNull SGeneralPlayerData player) {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        messageToAdapt = messageManager.adaptMessageFor(messageToAdapt, player);
        messageToAdapt = messageManager.adaptMessageFor(messageToAdapt, player.spongePlayer());
        return messageToAdapt;
    }

    @Override
    public @NotNull SingleConfigValue.Default<Component> configValue() {
        return CONFIG_VALUE;
    }

    @Override
    public @NotNull Collection<SPlaceHolder<?>> supportedPlaceholders() {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        List<SPlaceHolder<?>> placeHolders = new ArrayList<>();
        placeHolders.addAll(messageManager.placeholdersFor(SGeneralPlayerData.class));
        placeHolders.addAll(messageManager.placeholdersFor(Player.class));
        return new SingleUnmodifiableCollection<>(placeHolders);
    }
}
