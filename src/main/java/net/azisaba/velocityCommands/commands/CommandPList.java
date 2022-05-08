package net.azisaba.velocityCommands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.velocityCommands.player.PlayerListProvider;
import org.jetbrains.annotations.NotNull;

// non-global glist
public class CommandPList extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("plist")
                .requires(source -> source.hasPermission("velocitycommands.plist"))
                .executes(context -> CommandGList.execute("plist", context.getSource(), PlayerListProvider.velocity()))
                .then(literal("all")
                        .executes(context -> CommandGList.executeAll(context.getSource(), PlayerListProvider.velocity()))
                )
                .then(argument("servers", StringArgumentType.greedyString())
                        .suggests(suggestServers())
                        .executes(context -> CommandGList.executeMany(context.getSource(), PlayerListProvider.velocity(), StringArgumentType.getString(context, "servers").split("\s+")))
                );
    }
}
