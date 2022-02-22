package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.azisaba.velocityCommands.VelocityCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class AbstractCommand {
    public abstract LiteralArgumentBuilder<CommandSource> createBuilder();

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(createBuilder());
    }

    public static LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static <S> String getString(CommandContext<S> context, String name) {
        return StringArgumentType.getString(context, name);
    }

    public static SuggestionProvider<CommandSource> suggestPlayers() {
        return (source, builder) -> suggest(VelocityCommands.getProxy().getAllPlayers().stream().map(Player::getUsername), builder);
    }

    public static SuggestionProvider<CommandSource> suggestServers() {
        return (source, builder) ->
                suggest(
                        VelocityCommands.getProxy()
                                .getAllServers()
                                .stream()
                                .map(RegisteredServer::getServerInfo)
                                .map(ServerInfo::getName),
                        builder
                );
    }

    public static CompletableFuture<Suggestions> suggest(Stream<String> suggestions, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        suggestions.filter((suggestion) -> matchesSubStr(input, suggestion.toLowerCase(Locale.ROOT))).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static boolean matchesSubStr(String input, String suggestion) {
        for(int i = 0; !suggestion.startsWith(input, i); ++i) {
            i = suggestion.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    public static int sendMessageMissingPlayer(CommandSource source, String playerName) {
        source.sendMessage(Component.text("Player not found: " + playerName).color(NamedTextColor.RED));
        return 0;
    }

    public static int sendMessageMissingServer(CommandSource source, String serverName) {
        source.sendMessage(Component.text("Server not found: " + serverName).color(NamedTextColor.RED));
        return 0;
    }
}
