package net.azisaba.velocityCommands.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SimplePlayerInfo(@NotNull UUID uuid, @NotNull String username, @Nullable String childServer) {
}
