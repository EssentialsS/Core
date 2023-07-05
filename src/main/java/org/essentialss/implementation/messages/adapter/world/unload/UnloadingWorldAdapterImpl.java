package org.essentialss.implementation.messages.adapter.world.unload;

import net.kyori.adventure.text.Component;
import org.essentialss.implementation.EssentialsSMain;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.message.adapters.world.unload.UnloadingWorldMessageAdapter;
import org.essentialss.api.message.placeholder.SPlaceHolder;
import org.essentialss.api.message.placeholder.SPlaceHolders;
import org.essentialss.api.world.SWorldData;
import org.essentialss.implementation.config.value.modifiers.SingleDefaultConfigValueWrapper;
import org.essentialss.implementation.config.value.simple.ComponentConfigValue;
import org.essentialss.implementation.messages.adapter.AbstractMessageAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("i-am-message-adapter")
public class UnloadingWorldAdapterImpl extends AbstractMessageAdapter implements UnloadingWorldMessageAdapter {

    private static final SingleDefaultConfigValueWrapper<Component> CONFIG_VALUE;

    static {
        ComponentConfigValue messageConfigValue = new ComponentConfigValue("world", "unload", "Unloading");
        Component defaultMessage = Component.text("Creating world of " + SPlaceHolders.WORLD_NAME.formattedPlaceholderTag() + ".");
        CONFIG_VALUE = new SingleDefaultConfigValueWrapper<>(messageConfigValue, defaultMessage);
    }

    public UnloadingWorldAdapterImpl() {
        super(CONFIG_VALUE);
    }

    @NotNull
    @Override
    public Component adaptMessage(@NotNull Component messageToAdapt, @NotNull ServerWorld worldKey) {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        for (SPlaceHolder<ResourceKey> placeholder : messageManager.mappedPlaceholdersFor(ResourceKey.class)) {
            messageToAdapt = placeholder.copyWithTagType("world").apply(messageToAdapt, worldKey.key());
        }
        for (SPlaceHolder<ServerWorld> placeholder : messageManager.mappedPlaceholdersFor(ServerWorld.class)) {
            messageToAdapt = placeholder.apply(messageToAdapt, worldKey);
        }
        SWorldData worldData = EssentialsSMain.plugin().worldManager().get().dataFor(worldKey);
        for (SPlaceHolder<SWorldData> placeholder : messageManager.mappedPlaceholdersFor(SWorldData.class)) {
            messageToAdapt = placeholder.apply(messageToAdapt, worldData);
        }

        return messageToAdapt;
    }

    @NotNull
    @Override
    public SingleConfigValue.Default<Component> configValue() {
        return CONFIG_VALUE;
    }

    @NotNull
    @Override
    public Collection<SPlaceHolder<?>> supportedPlaceholders() {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        Collection<SPlaceHolder<?>> placeHolders = new ArrayList<>();
        placeHolders.addAll(messageManager.placeholdersFor(ResourceKey.class));
        placeHolders.addAll(messageManager.placeholdersFor(ServerWorld.class));
        placeHolders.addAll(messageManager.placeholdersFor(SWorldData.class));
        return placeHolders;
    }
}
