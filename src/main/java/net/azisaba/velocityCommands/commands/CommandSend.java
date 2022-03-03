package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.azisaba.velocityCommands.VelocityCommands;
import net.azisaba.velocityCommands.util.Util;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    private static void asyncConnectAndShowResults(CommandSource source, Collection<Player> players, RegisteredServer server) {
        source.sendMessage(Component.text("Attempting to send " + players.size() + " players to " + server.getServerInfo().getName()).color(NamedTextColor.GREEN));
        new Thread(() -> {
            List<ConnectionRequestBuilder.Status> results =
                    players.stream()
                            .map(p -> p.createConnectionRequest(server))
                            .map(ConnectionRequestBuilder::connect)
                            .map(CompletableFuture::join)
                            .map(ConnectionRequestBuilder.Result::getStatus)
                            .collect(Collectors.toList());
            showResults(source, results);
        }).start();
    }

    private static void showResults(CommandSource source, List<ConnectionRequestBuilder.Status> results) {
        int success = Util.count(results, ConnectionRequestBuilder.Status.SUCCESS);
        int alreadyConnecting = Util.count(results, ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS);
        int cancelled = Util.count(results, ConnectionRequestBuilder.Status.CONNECTION_CANCELLED);
        int failed = Util.count(results, ConnectionRequestBuilder.Status.SERVER_DISCONNECTED);
        int alreadyConnected = Util.count(results, ConnectionRequestBuilder.Status.ALREADY_CONNECTED);
        source.sendMessage(Component.text("Send Results:").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        source.sendMessage(Component.text("Success: " + success).color(NamedTextColor.GREEN));
        source.sendMessage(Component.text("Already connecting: " + alreadyConnecting).color(NamedTextColor.GREEN));
        source.sendMessage(Component.text("Cancelled: " + cancelled).color(NamedTextColor.GREEN));
        source.sendMessage(Component.text("Failed: " + failed).color(NamedTextColor.GREEN));
        source.sendMessage(Component.text("Already connected: " + alreadyConnected).color(NamedTextColor.GREEN));
    }

    private static int sendEveryoneToPlayer(CommandSource source, String playerName) {
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy()
                .getPlayer(playerName)
                .flatMap(Player::getCurrentServer)
                .map(ServerConnection::getServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingPlayer(source, playerName);
        }
        RegisteredServer server = optServer.get();
        Collection<Player> players = VelocityCommands.getProxy().getAllPlayers();
        asyncConnectAndShowResults(source, players, server);
        return players.size();
    }

    private static int sendEveryoneToServer(CommandSource source, String serverName) {
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy().getServer(serverName);
        if (!optServer.isPresent()) {
            return sendMessageMissingServer(source, serverName);
        }
        RegisteredServer server = optServer.get();
        Collection<Player> players = VelocityCommands.getProxy().getAllPlayers();
        asyncConnectAndShowResults(source, players, server);
        return players.size();
    }

    private static int sendCurrentToPlayer(CommandSource source, String playerName) {
        Player player = (Player) source;
        RegisteredServer currentServer = player.getCurrentServer().orElseThrow(IllegalStateException::new).getServer();
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy()
                .getPlayer(playerName)
                .flatMap(Player::getCurrentServer)
                .map(ServerConnection::getServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingPlayer(source, playerName);
        }
        RegisteredServer toServer = optServer.get();
        Collection<Player> players = currentServer.getPlayersConnected();
        asyncConnectAndShowResults(source, players, toServer);
        return players.size();
    }

    private static int sendCurrentToServer(CommandSource source, String serverName) {
        Player player = (Player) source;
        RegisteredServer currentServer = player.getCurrentServer().orElseThrow(IllegalStateException::new).getServer();
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy().getServer(serverName);
        if (!optServer.isPresent()) {
            return sendMessageMissingServer(source, serverName);
        }
        RegisteredServer toServer = optServer.get();
        Collection<Player> players = currentServer.getPlayersConnected();
        asyncConnectAndShowResults(source, players, toServer);
        return players.size();
    }

    private static int sendPlayerToPlayer(CommandSource source, String fromPlayer, String toPlayer) {
        Optional<Player> player = VelocityCommands.getProxy().getPlayer(fromPlayer);
        if (!player.isPresent()) {
            return sendMessageMissingPlayer(source, fromPlayer);
        }
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy()
                .getPlayer(toPlayer)
                .flatMap(Player::getCurrentServer)
                .map(ServerConnection::getServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingPlayer(source, toPlayer);
        }
        asyncConnectAndShowResults(source, Collections.singletonList(player.get()), optServer.get());
        return 1;
    }

    private static int sendPlayerToServer(CommandSource source, String fromPlayer, String toServer) {
        Optional<Player> player = VelocityCommands.getProxy().getPlayer(fromPlayer);
        if (!player.isPresent()) {
            return sendMessageMissingPlayer(source, fromPlayer);
        }
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy().getServer(toServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingServer(source, toServer);
        }
        source.sendMessage(Component.text("Attempting to send " + player.get().getUsername() + " to " + optServer.get().getServerInfo().getName()).color(NamedTextColor.GREEN));
        VelocityRedisBridge.getApi().sendPlayer(player.get(), optServer.get().getServerInfo().getName());
        //asyncConnectAndShowResults(source, Collections.singletonList(player.get()), optServer.get());
        return 1;
    }

    private static int sendServerToPlayer(CommandSource source, String fromServerName, String toPlayer) {
        Optional<RegisteredServer> fromServer = VelocityCommands.getProxy().getServer(fromServerName);
        if (!fromServer.isPresent()) {
            return sendMessageMissingServer(source, fromServerName);
        }
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy()
                .getPlayer(toPlayer)
                .flatMap(Player::getCurrentServer)
                .map(ServerConnection::getServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingPlayer(source, toPlayer);
        }
        asyncConnectAndShowResults(source, fromServer.get().getPlayersConnected(), optServer.get());
        return 1;
    }

    private static int sendServerToServer(CommandSource source, String fromServerName, String toServer) {
        Optional<RegisteredServer> fromServer = VelocityCommands.getProxy().getServer(fromServerName);
        if (!fromServer.isPresent()) {
            return sendMessageMissingServer(source, fromServerName);
        }
        Optional<RegisteredServer> optServer = VelocityCommands.getProxy().getServer(toServer);
        if (!optServer.isPresent()) {
            return sendMessageMissingServer(source, toServer);
        }
        asyncConnectAndShowResults(source, fromServer.get().getPlayersConnected(), optServer.get());
        return 1;
    }
}
