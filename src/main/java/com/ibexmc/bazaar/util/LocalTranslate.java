package com.ibexmc.bazaar.util;

import net.md_5.bungee.chat.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class LocalTranslate {

    private static Object language;
    private static Method getOrDefaultMethod;

    static {
        try {
            final Class<?> languageClass = Class.forName("net.minecraft.locale.LocaleLanguage");

            for (final Method method : languageClass.getDeclaredMethods()) {
                final int mod = method.getModifiers();

                // public static Language
                if (language == null
                        && Modifier.isPublic(mod)
                        && Modifier.isStatic(mod)
                        && method.getReturnType().equals(languageClass)) {
                    language = method.invoke(null);
                }

                // public String
                if (getOrDefaultMethod == null
                        && Modifier.isPublic(mod)
                        && !Modifier.isAbstract(mod)
                        && !Modifier.isStatic(mod)
                        && method.getReturnType().equals(String.class)
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].equals(String.class)) {
                    getOrDefaultMethod = method;
                }
            }
        } catch (final ClassNotFoundException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static String translate(final String key) {
        // Introduced in 1.17
        if (language != null && getOrDefaultMethod != null) {
            try {
                return (String) getOrDefaultMethod.invoke(language, key);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        // Introduced in 1.13
        return TranslationRegistry.INSTANCE.translate(key);
    }

    public static String getEnchantmentName(final Enchantment enchantment) {
        return LocalTranslate.translate("enchantment.minecraft." + enchantment.getKey().getKey());
    }

    public static String getItemName(final ItemStack item) {
        final Material material = item.getType();
        final String key = (material.isBlock() ? "block" : "item") + ".minecraft." + material.getKey().getKey();
        return LocalTranslate.translate(key);
    }
}