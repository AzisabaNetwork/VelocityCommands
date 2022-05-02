package net.azisaba.velocityCommands.util;

import com.google.common.hash.Hashing;
import com.velocitypowered.api.proxy.ServerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @NotNull
    public static String getIPAddress(@NotNull ServerConnection connection) {
        return connection.getServerInfo().getAddress().getAddress().getHostAddress();
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
}
