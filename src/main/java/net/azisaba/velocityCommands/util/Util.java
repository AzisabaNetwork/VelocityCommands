package net.azisaba.velocityCommands.util;

import com.google.common.hash.Hashing;
import com.velocitypowered.api.proxy.ServerConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Util {
    public static <T> int count(List<T> list, T value) {
        int i = 0;
        for (T t : list) {
            if (t == value) i++;
        }
        return i;
    }

    public static <T> int countEquals(List<T> list, T value) {
        int i = 0;
        for (T t : list) {
            if (Objects.equals(t, value)) i++;
        }
        return i;
    }

    @Nullable
    public static String getIPAddress(@NotNull ServerConnection connection) {
        return Optional.ofNullable(connection.getServerInfo().getAddress())
                .map(InetSocketAddress::getHostName)
                .map(hostName -> {
                    try {
                        return InetAddress.getByName(hostName);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(InetAddress::getHostAddress)
                .orElse(null);
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    public static String sha256(@NotNull String str) {
        return Hashing.sha256().hashBytes(str.getBytes()).toString();
    }

    @NotNull
    public static String capAt16(@NotNull String str) {
        return str.substring(0, Math.min(str.length(), 16));
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static <T, R> Function<T, R> memorize(@NotNull Function<T, R> function) {
        return new Function<>() {
            private final Map<T, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T t) {
                return cache.computeIfAbsent(t, function);
            }
        };
    }
}
