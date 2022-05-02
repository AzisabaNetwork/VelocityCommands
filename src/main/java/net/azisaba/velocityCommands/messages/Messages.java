package net.azisaba.velocityCommands.messages;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {
    private static final Yaml YAML = new Yaml();
    private static final Map<String, MessageInstance> LOCALES = new ConcurrentHashMap<>();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .extractUrls()
                    .hexColors()
//                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
    private static MessageInstance fallback;

    public static void load() throws IOException {
        fallback = Objects.requireNonNullElse(load(Locale.ENGLISH.getLanguage()), MessageInstance.FALLBACK);
        for (String language : Locale.getISOLanguages()) {
            var instance = Messages.load(language);
            if (instance != null) {
                LOCALES.put(language, instance);
            } else {
                LOCALES.put(language, fallback);
            }
        }
    }

    @Nullable
    public static MessageInstance load(@NotNull String language) throws IOException {
        try (InputStream in = Messages.class.getResourceAsStream("/messages_" + language + ".yml")) {
            if (in == null) {
                return null;
            }
            Map<Object, Object> map = YAML.load(in);
            return new MessageInstance(s -> String.valueOf(map.get(s)));
        }
    }

    @NotNull
    public static MessageInstance getInstance(@Nullable Locale locale) {
        Objects.requireNonNull(fallback, "messages not loaded yet");
        if (locale == null) {
            return fallback;
        }
        return LOCALES.getOrDefault(locale.getLanguage(), fallback);
    }

    @NotNull
    public static Component format(@NotNull String s, Object... args) {
        return LEGACY_COMPONENT_SERIALIZER.deserialize(s.formatted(args));
    }

    public static void sendFormatted(@NotNull CommandSource source, @NotNull String key, Object @NotNull ... args) {
        Locale locale = Locale.ENGLISH;
        if (source instanceof Player) {
            locale = ((Player) source).getEffectiveLocale();
        }
        String rawMessage = getInstance(locale).get(key);
        Component formatted = format(rawMessage, args);
        source.sendMessage(formatted);
    }
}
