package org.essentialss.implementation.module;

import org.essentialss.api.utils.Singleton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class EssentialsSModules {

    public static Singleton<Optional<Object>> LEGACY_CHAT_FORMATTING = new Singleton<>(() -> {
        try {
            Class<?> clazz = Class.forName("org.essentialss.module.chat.LegacyChatModule");
            Method canBootMethod = clazz.getDeclaredMethod("canBoot");
            if (!(Boolean) canBootMethod.invoke(null)) {
                return Optional.empty();
            }
            Object instance = clazz.getDeclaredConstructor().newInstance();
            return Optional.of(instance);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    });

}
