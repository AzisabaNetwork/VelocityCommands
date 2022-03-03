package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;

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
        VelocityRedisBridge.getApi().sendRawMessageToAll(json);
        return 0;
    }
}
