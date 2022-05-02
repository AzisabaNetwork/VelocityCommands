package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.messages.Messages;
import net.azisaba.velocityCommands.util.Util;
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
        Optional<PlayerInfo> opt = VelocityRedisBridge.getApi().getPlayerInfo(player.getUniqueId());
        if (opt.isEmpty()) {
            throw new AssertionError();
        }
        PlayerInfo info = opt.get();
        Messages.sendFormatted(source, "command.whereami.server_id", info.getChildServer());
        Messages.sendFormatted(source, "command.whereami.proxy_id", info.getProxyServer());
        String hashedServer = player.getCurrentServer().map(Util::getIPAddress).map(Util::sha256).map(Util::capAt16).orElse("null");
        Messages.sendFormatted(source, "command.whereami.server_host", hashedServer);
        return 1;
    }
}
