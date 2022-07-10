package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.velocityCommands.player.PlayerListProvider;
import net.azisaba.velocityCommands.player.SimplePlayerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CommandGList extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("glist")
                .requires(source -> source.hasPermission("velocitycommands.glist"))
                .executes(context -> execute("glist", context.getSource(), PlayerListProvider.velocityRedisBridge()))
                .then(literal("all")
                        .executes(context -> executeAll(context.getSource(), PlayerListProvider.velocityRedisBridge()))
                )
                .then(argument("servers", StringArgumentType.greedyString())
                        .suggests(suggestServers())
                        .executes(context -> executeMany(context.getSource(), PlayerListProvider.velocityRedisBridge(), StringArgumentType.getString(context, "servers").split("\s+")))
                );
    }

    public static int execute(String command, CommandSource source, PlayerListProvider provider) {
        source.sendMessage(Component.empty()
                .append(Component.text(provider.get().size(), NamedTextColor.GREEN))
                .append(Component.text(" players are currently online.", NamedTextColor.YELLOW)));
        source.sendMessage(Component.text("To view all players on servers, use /" + command + " all.", NamedTextColor.YELLOW));
        return 0;
    }

    public static int executeAll(CommandSource source, PlayerListProvider provider) {
        List<SimplePlayerInfo> players = provider.get();
        players.stream()
                .map(SimplePlayerInfo::childServer)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(server -> executeSingleServer(source, () -> players, server));
        players.stream()
                .map(SimplePlayerInfo::childServer)
                .filter(Objects::isNull)
                .forEach(server -> executeSingleServer(source, () -> players, null));
        source.sendMessage(Component.empty()
                .append(Component.text(players.size(), NamedTextColor.GREEN))
                .append(Component.text(" players are currently online.", NamedTextColor.YELLOW)));
        return 0;
    }

    public static int executeMany(CommandSource source, PlayerListProvider provider, String[] servers) {
        List<SimplePlayerInfo> players = provider.get();
        int total = 0;
        for (String server : servers) {
            total += executeSingleServer(source, () -> players, server);
        }
        source.sendMessage(Component.empty()
                .append(Component.text(total, NamedTextColor.GREEN))
                .append(Component.text(" players are currently online.", NamedTextColor.YELLOW)));
        return total;
    }

    public static int executeSingleServer(CommandSource source, PlayerListProvider provider, @Nullable String server) {
        List<TextComponent> filtered = provider.get()
                .stream()
                .filter(player -> Objects.equals(server, player.childServer()))
                .map(SimplePlayerInfo::username)
                .sorted()
                .map(Component::text)
                .toList();
        String serverName = server;
        if (serverName == null) {
            serverName = "<null>";
        }
        source.sendMessage(Component.empty()
                .append(Component.text("[" + serverName + "] ", NamedTextColor.DARK_AQUA))
                .append(Component.text("(" + filtered.size() + ")", NamedTextColor.GRAY))
                .append(Component.text(": "))
                .append(Component.join(Component.text(", "), filtered)));
        return filtered.size();
    }
}
