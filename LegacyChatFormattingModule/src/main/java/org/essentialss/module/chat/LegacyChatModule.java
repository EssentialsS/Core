package org.essentialss.module.chat;

import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

public class LegacyChatModule {

    public void boot(PluginContainer container) {
        Sponge.eventManager().registerListeners(container, new ChatListener());
    }

    public static boolean canBoot() {
        try {
            Class.forName("org.spongepowered.api.event.message.PlayerChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
