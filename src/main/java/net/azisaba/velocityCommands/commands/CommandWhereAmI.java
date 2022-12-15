package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.messages.Messages;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.azisaba.velocityredisbridge.util.PlayerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CommandWhereAmI extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("whereami")
                .requires(source -> source.hasPermission("velocitycommands.whereami") && source instanceof Player)
                .executes(context -> execute(context.getSource()));
    }

    private static int execute(CommandSource source) {
        if (!(source instanceof Player player)) {
            throw new AssertionError();
        }
        Optional<PlayerInfo> playerInfoOpt = VelocityRedisBridge.getApi().getPlayerInfo(player.getUniqueId());
        if (playerInfoOpt.isEmpty()) {
            throw new AssertionError();
        }
        PlayerInfo playerInfo = playerInfoOpt.get();
        Messages.sendFormatted(source, "command.whereami.server_id", playerInfo.getChildServer());
        Messages.sendFormatted(source, "command.whereami.proxy_id", System.getenv("POD_NAME"));
        Messages.sendFormatted(source, "command.whereami.proxy_host", System.getenv("NODE_NAME"));
        return 1;
    }
}
