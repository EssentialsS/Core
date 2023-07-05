package org.essentialss.messages.adapter.player.listener.spy;

import net.kyori.adventure.text.Component;
import org.essentialss.EssentialsSMain;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.message.adapters.player.listener.spy.CommandSpyMessageAdapter;
import org.essentialss.api.message.placeholder.SPlaceHolder;
import org.essentialss.api.message.placeholder.SPlaceHolders;
import org.essentialss.api.player.data.SGeneralPlayerData;
import org.essentialss.config.value.modifiers.SingleDefaultConfigValueWrapper;
import org.essentialss.config.value.simple.ComponentConfigValue;
import org.essentialss.messages.adapter.AbstractEnabledMessageAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("i-am-message-adapter")
public class CommandSpyMessageAdapterImpl extends AbstractEnabledMessageAdapter implements CommandSpyMessageAdapter {

    private static final SingleDefaultConfigValueWrapper<Component> CONFIG_VALUE;

    static {
        ComponentConfigValue messageAdapter = new ComponentConfigValue("player", "spy", "command", "Message");
        Component defaultMessage = Component.text(
                SPlaceHolders.PLAYER_NICKNAME.formattedPlaceholderTag() + " used the command of /" + SPlaceHolders.COMMAND_FULL.formattedPlaceholderTag());
        CONFIG_VALUE = new SingleDefaultConfigValueWrapper<>(messageAdapter, defaultMessage);
    }

    public CommandSpyMessageAdapterImpl() {
        super(true, CONFIG_VALUE);
    }

    @Override
    public @NotNull Component adaptMessage(@NotNull Component messageToAdapt,
                                           @NotNull Subject commandSender,
                                           @NotNull String command,
                                           @NotNull String arguments) {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        messageToAdapt = messageManager.adaptMessageFor(messageToAdapt, commandSender);

        if (commandSender instanceof Player) {
            SGeneralPlayerData playerData = EssentialsSMain.plugin().playerManager().get().dataFor((Player) commandSender);
            messageToAdapt = messageManager.adaptMessageFor(messageToAdapt, playerData);
        }

        messageToAdapt = SPlaceHolders.COMMAND_MAIN.apply(messageToAdapt, command);
        messageToAdapt = SPlaceHolders.COMMAND_ARGUMENTS.apply(messageToAdapt, arguments);

        String combined = command + " " + arguments;
        messageToAdapt = messageManager.adaptMessageFor(messageToAdapt, combined);
        return messageToAdapt;
    }

    @Override
    public @NotNull SingleConfigValue.Default<Component> configValue() {
        return CONFIG_VALUE;
    }

    @Override
    public @NotNull Collection<SPlaceHolder<?>> supportedPlaceholders() {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();

        Collection<SPlaceHolder<?>> placeholders = new LinkedList<>();
        placeholders.addAll(messageManager.placeholdersFor(Subject.class));
        placeholders.addAll(messageManager.placeholdersFor(SGeneralPlayerData.class));
        placeholders.addAll(messageManager.placeholdersFor(SPlaceHolders.COMMAND));
        return placeholders;
    }
}
