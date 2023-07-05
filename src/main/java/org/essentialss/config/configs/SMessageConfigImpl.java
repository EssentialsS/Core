package org.essentialss.config.configs;

import org.essentialss.EssentialsSMain;
import org.essentialss.api.config.configs.MessageConfig;
import org.essentialss.api.config.value.ConfigValue;
import org.essentialss.api.message.MessageAdapters;
import org.essentialss.api.message.adapters.MessageAdapter;
import org.essentialss.api.message.adapters.vanilla.VanillaMessageAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SMessageConfigImpl implements MessageConfig {

    private final @NotNull Locale locale;

    public SMessageConfigImpl(@NotNull Locale locale) {
        this.locale = locale;
    }

    @Override
    public @NotNull Collection<ConfigValue<?>> expectedNodes() {
        return EssentialsSMain.plugin().messageManager().get().adapters().all().map(MessageAdapter::configValue).collect(Collectors.toList());
    }

    @Override
    public @NotNull File file() {
        File folder = Sponge.configManager().pluginConfig(EssentialsSMain.plugin().container()).directory().toFile();
        return new File(folder, "message/" + Locale.ENGLISH.toLanguageTag() + ".conf");
    }

    @Override
    public void update() throws ConfigurateException, SerializationException {
        this.update(EssentialsSMain.plugin().messageManager().get().adapters());
    }

    @Override
    public @NotNull Locale locale() {
        return this.locale;
    }

    public void update(@NotNull MessageAdapters messageAdapters) throws ConfigurateException, SerializationException {
        ConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoader();
        ConfigurationNode root = loader.load();

        List<MessageAdapter> adapters = messageAdapters.all().collect(Collectors.toList());
        for (MessageAdapter adapter : adapters) {
            adapter.configValue().setDefaultIfNotPresent(root);
            if (adapter instanceof MessageAdapter.Enabled) {
                MessageAdapter.Enabled enabledMessageAdapter = (MessageAdapter.Enabled) adapter;
                enabledMessageAdapter.enabledValue().setDefaultIfNotPresent(root);
            }
            if (adapter instanceof VanillaMessageAdapter) {
                VanillaMessageAdapter vanillaMessageAdapter = (VanillaMessageAdapter) adapter;
                vanillaMessageAdapter.useVanilla().setDefaultIfNotPresent(root);
            }
        }
        loader.save(root);
    }


}
