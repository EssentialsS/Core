package org.essentialss.implementation.messages.adapter.world.load;

import net.kyori.adventure.text.Component;
import org.essentialss.implementation.EssentialsSMain;
import org.essentialss.api.config.value.SingleConfigValue;
import org.essentialss.api.message.MessageManager;
import org.essentialss.api.message.adapters.world.load.LoadedWorldMessageAdapter;
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
public class LoadedWorldAdapterImpl extends AbstractMessageAdapter implements LoadedWorldMessageAdapter {

    private static final SingleDefaultConfigValueWrapper<Component> CONFIG_VALUE;

    static {
        ComponentConfigValue messageConfigValue = new ComponentConfigValue("world", "load", "Loaded");
        Component defaultMessage = Component.text("Loading world of " + SPlaceHolders.WORLD_NAME.formattedPlaceholderTag() + ".");
        CONFIG_VALUE = new SingleDefaultConfigValueWrapper<>(messageConfigValue, defaultMessage);
    }

    public LoadedWorldAdapterImpl() {
        super(CONFIG_VALUE);
    }

    @NotNull
    @Override
    public Component adaptMessage(@NotNull Component messageToAdapt, @NotNull ServerWorld world) {
        MessageManager messageManager = EssentialsSMain.plugin().messageManager().get();
        for (SPlaceHolder<ResourceKey> placeholder : messageManager.mappedPlaceholdersFor(ResourceKey.class)) {
            messageToAdapt = placeholder.copyWithTagType("world").apply(messageToAdapt, world.key());
        }
        for (SPlaceHolder<ServerWorld> placeholder : messageManager.mappedPlaceholdersFor(ServerWorld.class)) {
            messageToAdapt = placeholder.apply(messageToAdapt, world);
        }
        SWorldData worldData = EssentialsSMain.plugin().worldManager().get().dataFor(world.key());
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
        placeHolders.addAll(messageManager.placeholdersFor(SWorldData.class));
        return placeHolders;
    }
}
