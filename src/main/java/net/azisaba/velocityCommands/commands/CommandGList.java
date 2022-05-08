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

import java.util.List;

public class CommandGList extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("glist")
                .requires(source -> source.hasPermission("velocitycommands.glist"))
                .executes(context -> execute("glist", context.getSource(), PlayerListProvider.velocityRedisBridge()))
                .then(literal("all")
                        .executes(context -> executeAll(context.getSource(), PlayerListProvider.velocityRedisBridge()))
                )
                .then(argument("server", StringArgumentType.word())
                        .suggests(suggestServers())
                        .executes(context -> executeSingleServer(context.getSource(), PlayerListProvider.velocityRedisBridge(), StringArgumentType.getString(context, "server")))
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
                .distinct()
                .sorted()
                .forEach(server -> executeSingleServer(source, () -> players, server));
        source.sendMessage(Component.empty()
                .append(Component.text(players.size(), NamedTextColor.GREEN))
                .append(Component.text(" players are currently online.", NamedTextColor.YELLOW)));
        return 0;
    }

    public static int executeSingleServer(CommandSource source, PlayerListProvider provider, String server) {
        List<TextComponent> filtered = provider.get()
                .stream()
                .filter(player -> player.childServer().equals(server))
                .map(SimplePlayerInfo::username)
                .map(Component::text)
                .toList();
        source.sendMessage(Component.empty()
                .append(Component.text("[" + server + "] ", NamedTextColor.DARK_AQUA))
                .append(Component.text("(" + filtered.size() + ")", NamedTextColor.GRAY))
                .append(Component.text(": "))
                .append(Component.join(Component.text(", "), filtered)));
        return filtered.size();
    }
}
