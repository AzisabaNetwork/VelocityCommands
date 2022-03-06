package net.azisaba.velocityCommands;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.velocityCommands.commands.CommandAlert;
import net.azisaba.velocityCommands.commands.CommandAlertRaw;
import net.azisaba.velocityCommands.commands.CommandFind;
import net.azisaba.velocityCommands.commands.CommandSend;
import org.slf4j.Logger;

import java.util.Objects;

@Plugin(id = "velocitycommands",
        name = "VelocityCommands",
        version = "dev",
        dependencies = {@Dependency(id = "velocity-redis-bridge")})
public class VelocityCommands {
    private static VelocityCommands instance;
    private final ProxyServer server;

    @SuppressWarnings("unused") // TODO: do i need logger in parameter?
    @Inject
    public VelocityCommands(ProxyServer server, Logger logger) {
        instance = this;
        this.server = server;
    }

    @Subscribe(order = PostOrder.LATE)
    public void onProxyInitialization(ProxyInitializeEvent e) {
        server.getCommandManager().register(new CommandAlert().createCommand()); // /alert
        server.getCommandManager().register(new CommandAlertRaw().createCommand()); // /alertraw
        server.getCommandManager().register(new CommandSend().createCommand()); // /send
        server.getCommandManager().register(new CommandFind().createCommand()); // /find
    }

    public ProxyServer getServer() {
        return server;
    }

    public static VelocityCommands getInstance() {
        return Objects.requireNonNull(instance, "plugin is not initialized");
    }

    public static ProxyServer getProxy() {
        return getInstance().getServer();
    }
}
