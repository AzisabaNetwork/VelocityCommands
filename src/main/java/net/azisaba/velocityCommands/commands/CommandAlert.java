package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;

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
        VelocityRedisBridge.getApi().sendMessageToAll("\u00a78[\u00a74Alert\u00a78] \u00a7f" + message.replace('&', '\u00a7'));
        /*
        for (Player player : VelocityCommands.getProxy().getAllPlayers()) {
            player.sendMessage(
                    Component.text("")
                            .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text("Alert").color(NamedTextColor.DARK_RED))
                            .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                            .append(LegacyComponentSerializer.legacyAmpersand().deserialize(message).color(NamedTextColor.WHITE))
            );
            count++;
        }
        */
        return count;
    }
}
