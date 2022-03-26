package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.azisaba.velocityredisbridge.util.PlayerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class CommandSend extends AbstractCommand {
    @Override
    public LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("send")
                .requires(source -> source.hasPermission("velocitycommands.send"))
                .then(literal("everyone")
                        .then(literal("to")
                                .then(literal("player")
                                        .then(argument("to_player", StringArgumentType.string())
                                                .suggests(suggestPlayers())
                                                .executes(context -> sendEveryoneToPlayer(context.getSource(), getString(context, "to_player")))
                                        )
                                )
                                .then(literal("server")
                                        .then(argument("to_server", StringArgumentType.string())
                                                .suggests(suggestServers())
                                                .executes(context -> sendEveryoneToServer(context.getSource(), getString(context, "to_server")))
                                        )
                                )
                        )
                )
                .then(literal("current")
                        .requires(source -> source instanceof Player)
                        .then(literal("to")
                                .then(literal("player")
                                        .then(argument("to_player", StringArgumentType.string())
                                                .suggests(suggestPlayers())
                                                .executes(context -> sendCurrentToPlayer(context.getSource(), getString(context, "to_player")))
                                        )
                                )
                                .then(literal("server")
                                        .then(argument("to_server", StringArgumentType.string())
                                                .suggests(suggestServers())
                                                .executes(context -> sendCurrentToServer(context.getSource(), getString(context, "to_server")))
                                        )
                                )
                        )
                )
                .then(literal("player")
                        .then(argument("from_player", StringArgumentType.string())
                                .suggests(suggestPlayers())
                                .then(literal("to")
                                        .then(literal("player")
                                                .then(argument("to_player", StringArgumentType.string())
                                                        .suggests(suggestPlayers())
                                                        .executes(context -> sendPlayerToPlayer(context.getSource(), getString(context, "from_player"), getString(context, "to_player")))
                                                )
                                        )
                                        .then(literal("server")
                                                .then(argument("to_server", StringArgumentType.string())
                                                        .suggests(suggestServers())
                                                        .executes(context -> sendPlayerToServer(context.getSource(), getString(context, "from_player"), getString(context, "to_server")))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("from")
                        .then(argument("from_server", StringArgumentType.string())
                                .suggests(suggestServers())
                                .then(literal("to")
                                        .then(literal("player")
                                                .then(argument("to_player", StringArgumentType.string())
                                                        .suggests(suggestPlayers())
                                                        .executes(context -> sendServerToPlayer(context.getSource(), getString(context, "from_server"), getString(context, "to_player")))
                                                )
                                        )
                                        .then(literal("server")
                                                .then(argument("to_server", StringArgumentType.string())
                                                        .suggests(suggestServers())
                                                        .executes(context -> sendServerToServer(context.getSource(), getString(context, "from_server"), getString(context, "to_server")))
                                                )
                                        )
                                )
                        )
                );
    }

    private static int sendEveryoneToPlayer(CommandSource source, String playerName) {
        String toServer = VelocityRedisBridge.getApi().getPlayerInfo(playerName).map(PlayerInfo::getChildServer).orElse(null);
        if (toServer == null) {
            return sendMessageMissingPlayer(source, playerName);
        }
        long count = VelocityRedisBridge.getApi().getAllPlayerInfo()
                .stream()
                .peek(p -> VelocityRedisBridge.getApi().sendPlayer(p.getUsername(), toServer))
                .count();
        source.sendMessage(Component.text("Attempting to send " + count + " players to " + toServer).color(NamedTextColor.GREEN));
        return (int) count;
    }

    private static int sendEveryoneToServer(CommandSource source, String serverName) {
        long count = VelocityRedisBridge.getApi()
                .getAllPlayerInfo()
                .stream()
                .peek(p -> VelocityRedisBridge.getApi().sendPlayer(p.getUsername(), serverName))
                .count();
        source.sendMessage(Component.text("Attempting to send " + count + " players to " + serverName).color(NamedTextColor.GREEN));
        return (int) count;
    }

    private static int sendCurrentToPlayer(CommandSource source, String playerName) {
        Player player = (Player) source;
        String currentServer = player.getCurrentServer().orElseThrow(IllegalStateException::new).getServer().getServerInfo().getName();
        return sendServerToPlayer(source, currentServer, playerName);
    }

    private static int sendCurrentToServer(CommandSource source, String serverName) {
        Player player = (Player) source;
        String fromServer = player.getCurrentServer().orElseThrow(IllegalStateException::new).getServer().getServerInfo().getName();
        return sendServerToServer(source, fromServer, serverName);
    }

    private static int sendPlayerToPlayer(CommandSource source, String fromPlayer, String toPlayer) {
        String toServer = VelocityRedisBridge.getApi().getPlayerInfo(toPlayer).map(PlayerInfo::getChildServer).orElse(null);
        if (toServer == null) {
            return sendMessageMissingPlayer(source, toPlayer);
        }
        VelocityRedisBridge.getApi().sendPlayer(fromPlayer, toServer);
        source.sendMessage(Component.text("Attempting to send " + fromPlayer + " to " + toServer).color(NamedTextColor.GREEN));
        return 1;
    }

    private static int sendPlayerToServer(CommandSource source, String fromPlayer, String toServer) {
        Optional<PlayerInfo> player = VelocityRedisBridge.getApi().getPlayerInfo(fromPlayer);
        if (!player.isPresent()) {
            return sendMessageMissingPlayer(source, fromPlayer);
        }
        source.sendMessage(Component.text("Attempting to send " + player.get().getUsername() + " to " + toServer).color(NamedTextColor.GREEN));
        VelocityRedisBridge.getApi().sendPlayer(player.get().getUsername(), toServer);
        return 1;
    }

    private static int sendServerToPlayer(CommandSource source, String fromServerName, String toPlayer) {
        String toServer = VelocityRedisBridge.getApi().getPlayerInfo(toPlayer).map(PlayerInfo::getChildServer).orElse(null);
        if (toServer == null) {
            return sendMessageMissingPlayer(source, toPlayer);
        }
        return sendServerToServer(source, fromServerName, toPlayer);
    }

    private static int sendServerToServer(CommandSource source, String fromServerName, String toServer) {
        long count = VelocityRedisBridge.getApi().getAllPlayerInfo().stream()
                .filter(p -> p.getChildServer().equalsIgnoreCase(fromServerName))
                .peek(pi -> VelocityRedisBridge.getApi().sendPlayer(pi.getUsername(), toServer))
                .count();
        source.sendMessage(Component.text("Attempting to send " + count + " players to " + toServer).color(NamedTextColor.GREEN));
        return (int) count;
    }
}
