package net.azisaba.velocityCommands.messages;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MessageInstance {
    public static final MessageInstance FALLBACK = new Fallback();

    private final @NotNull Function<@NotNull String, @NotNull String> getter;

    @Contract(pure = true)
    public MessageInstance(@NotNull Function<@NotNull String, @NotNull String> getter) {
        this.getter = getter;
    }

    public @NotNull String get(@NotNull String key) {
        return getter.apply(key);
    }

    private static class Fallback extends MessageInstance {
        private Fallback() {
            super(Function.identity());
        }

        @Contract(value = "_ -> param1", pure = true)
        @Override
        public @NotNull String get(@NotNull String key) {
            return key;
        }
    }
}
