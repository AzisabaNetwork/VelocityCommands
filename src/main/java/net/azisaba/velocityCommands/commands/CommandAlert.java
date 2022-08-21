package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.VelocityCommands;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class CommandAlert extends AbstractCommand {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER =
            LegacyComponentSerializer.builder().character('&').extractUrls().hexColors().build();

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("alert")
                .requires(source -> source.hasPermission("velocitycommands.alert"))
                .then(argument("global", BoolArgumentType.bool())
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(context -> alert(StringArgumentType.getString(context, "message"), BoolArgumentType.getBool(context, "global")))
                        )
                )
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> alert(StringArgumentType.getString(context, "message"), true))
                );
    }

    private static int alert(String message, boolean global) {
        if (global) {
            VelocityRedisBridge.getApi().sendMessageToAll("\u00a78[\u00a74Alert\u00a78] \u00a7f" + message.replace('&', '\u00a7'));
        } else {
            for (Player player : VelocityCommands.getProxy().getAllPlayers()) {
                player.sendMessage(
                        Component.text("")
                                .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                                .append(Component.text("Alert").color(NamedTextColor.DARK_RED))
                                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                                .append(LEGACY_COMPONENT_SERIALIZER.deserialize(message).color(NamedTextColor.WHITE))
                );
            }
        }
        return 0;
    }
}
