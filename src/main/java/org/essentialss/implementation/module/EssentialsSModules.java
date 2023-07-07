package org.essentialss.implementation.module;

import org.essentialss.api.utils.Singleton;
import org.essentialss.module.chat.LegacyChatModule;

import java.util.Optional;

public class EssentialsSModules {

    public static Singleton<Optional<LegacyChatModule>> LEGACY_CHAT_FORMATTING = new Singleton<>(
            () -> LegacyChatModule.canBoot() ? Optional.of(new LegacyChatModule()) : Optional.empty());

}
