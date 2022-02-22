package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.VelocityCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandAlert extends AbstractCommand {
    @Override
    public LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("alert")
                .requires(source -> source.hasPermission("velocitycommands.alert"))
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> alert(StringArgumentType.getString(context, "message")))
                );
    }

    private static int alert(String message) {
        int count = 0;
        for (Player player : VelocityCommands.getProxy().getAllPlayers()) {
            player.sendMessage(
                    Component.text("")
                            .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text("Alert").color(NamedTextColor.DARK_RED))
                            .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(message).color(NamedTextColor.WHITE))
            );
            count++;
        }
        return count;
    }
}
