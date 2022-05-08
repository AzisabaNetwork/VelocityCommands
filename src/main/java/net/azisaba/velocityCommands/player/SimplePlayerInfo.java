package net.azisaba.velocityCommands.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SimplePlayerInfo(@NotNull UUID uuid, @NotNull String username, @NotNull String childServer) {
}
