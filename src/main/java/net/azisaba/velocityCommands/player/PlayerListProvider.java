package net.azisaba.velocityCommands.player;

import net.azisaba.velocityCommands.VelocityCommands;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public interface PlayerListProvider extends Supplier<List<SimplePlayerInfo>> {
    @Override
    @NotNull
    List<SimplePlayerInfo> get();

    @Contract(pure = true)
    static @NotNull PlayerListProvider velocityRedisBridge() {
        return () -> VelocityRedisBridge.getApi()
                .getAllPlayerInfo()
                .stream()
                .map(p -> new SimplePlayerInfo(p.getUuid(), p.getUsername(), p.getChildServer()))
                .toList();
    }

    @Contract(pure = true)
    static @NotNull PlayerListProvider velocity() {
        return () -> VelocityCommands.getProxy()
                .getAllPlayers()
                .stream()
                .map(p -> new SimplePlayerInfo(p.getUniqueId(), p.getUsername(), p.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse("null")))
                .toList();
    }
}
