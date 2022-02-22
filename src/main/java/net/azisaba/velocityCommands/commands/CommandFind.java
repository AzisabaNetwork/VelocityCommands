package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.azisaba.velocityCommands.VelocityCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class CommandFind extends AbstractCommand {
    @Override
    public LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("find")
                .requires(source -> source.hasPermission("velocitycommands.find"))
                .then(argument("player", StringArgumentType.greedyString())
                        .suggests(suggestPlayers())
                        .executes(context -> find(context.getSource(), StringArgumentType.getString(context, "player")))
                );
    }

    private static int find(CommandSource source, String player) {
        Optional<ServerInfo> serverInfo = VelocityCommands.getProxy().getPlayer(player)
                .flatMap(Player::getCurrentServer)
                .map(ServerConnection::getServerInfo);
        if (!serverInfo.isPresent()) {
            return sendMessageMissingPlayer(source, player);
        }
        source.sendMessage(
                Component.text("")
                        .append(Component.text(player).color(NamedTextColor.GREEN))
                        .append(Component.text(" is online at "))
                        .append(Component.text(serverInfo.get().getName()).color(NamedTextColor.YELLOW))
        );
        return 1;
    }
}
