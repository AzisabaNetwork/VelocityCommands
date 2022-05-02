package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.velocityCommands.VelocityCommands;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class CommandAlertRaw extends AbstractCommand {
    @Override
    public LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("alertraw")
                .requires(source -> source.hasPermission("velocitycommands.alertraw"))
                .then(argument("global", BoolArgumentType.bool())
                        .then(argument("json", StringArgumentType.greedyString())
                                .executes(context -> alert(StringArgumentType.getString(context, "json"), BoolArgumentType.getBool(context, "global")))
                        )
                )
                .then(argument("json", StringArgumentType.greedyString())
                        .executes(context -> alert(StringArgumentType.getString(context, "json"), true))
                );
    }

    private static int alert(String json, boolean global) {
        if (global) {
            VelocityRedisBridge.getApi().sendRawMessageToAll(json);
        } else {
            for (Player player : VelocityCommands.getProxy().getAllPlayers()) {
                player.sendMessage(GsonComponentSerializer.gson().deserializeOr(json, Component.text(json)));
            }
        }
        return 0;
    }
}
