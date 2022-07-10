package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.azisaba.velocityCommands.VelocityCommands;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CommandReconnect extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("reconnect")
                .requires(source -> source.hasPermission("velocitycommands.reconnect") && source instanceof Player)
                .then(argument("lobby", StringArgumentType.string())
                        .suggests(suggestServers())
                        .executes(ctx -> execute((Player) ctx.getSource(), StringArgumentType.getString(ctx, "lobby")))
                );
    }

    private static int execute(@NotNull Player player, @NotNull String lobby) {
        Optional<RegisteredServer> server = VelocityCommands.getProxy().getServer(lobby);
        if (server.isEmpty()) {
            return sendMessageMissingServer(player, lobby);
        }
        RegisteredServer currentServer = player.getCurrentServer().orElseThrow(IllegalStateException::new).getServer();
        player.createConnectionRequest(server.get()).connectWithIndication().thenAccept(result -> {
            if (result) {
                player.createConnectionRequest(currentServer).fireAndForget();
            }
        });
        return 1;
    }
}
