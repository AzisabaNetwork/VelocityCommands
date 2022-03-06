package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.azisaba.velocityredisbridge.util.PlayerInfo;
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
        Optional<PlayerInfo> playerInfo = VelocityRedisBridge.getApi().getPlayerInfo(player);
        if (!playerInfo.isPresent()) {
            return sendMessageMissingPlayer(source, player);
        }
        source.sendMessage(
                Component.text("")
                        .append(Component.text(player).color(NamedTextColor.GREEN))
                        .append(Component.text(" is online in "))
                        .append(Component.text(playerInfo.get().getChildServer()).color(NamedTextColor.YELLOW))
                        .append(Component.text(" at proxy "))
                        .append(Component.text(playerInfo.get().getProxyServer()).color(NamedTextColor.GOLD))
        );
        return 1;
    }
}
