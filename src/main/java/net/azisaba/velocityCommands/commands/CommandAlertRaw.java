package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.VelocityCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class CommandAlertRaw extends AbstractCommand {
    @Override
    public LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("alertraw")
                .requires(source -> source.hasPermission("velocitycommands.alertraw"))
                .then(argument("json", StringArgumentType.greedyString())
                        .executes(context -> alert(StringArgumentType.getString(context, "json")))
                );
    }

    private static int alert(String json) {
        Component message;
        try {
            message = GsonComponentSerializer.gson().deserialize(json);
        } catch (RuntimeException e) {
            message = Component.text(json);
        }
        int count = 0;
        for (Player player : VelocityCommands.getProxy().getAllPlayers()) {
            player.sendMessage(message);
            count++;
        }
        return count;
    }
}
