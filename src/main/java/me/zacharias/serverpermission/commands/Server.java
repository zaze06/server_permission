package me.zacharias.serverpermission.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.zacharias.serverpermission.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import static me.zacharias.serverpermission.ServerPermission.*;

public class Server implements Command {
    @Override
    public String getCommand() {
        return "Server";
    }

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player p) {
            if (invocation.arguments().length > 0) {
                if (p.hasPermission("serverPermission.server." + invocation.arguments()[0])) {
                    Optional<RegisteredServer> server = SERVER.getServer(invocation.arguments()[0]);
                    if(server.isEmpty()){
                        p.sendMessage(MiniMessage.miniMessage().deserialize("<red> Server "+invocation.arguments()[0]+" doesn't exist"));
                    }
                    RegisteredServer registeredServer = server.get();
                    CompletableFuture<Boolean> booleanCompletableFuture = p.createConnectionRequest(registeredServer).connectWithIndication();
                    String name = registeredServer.getServerInfo().getName();
                    try {
                        if(booleanCompletableFuture.get()){
                            p.sendMessage(MiniMessage.miniMessage().deserialize("successfully connected you to "+ name));
                        }else{
                            p.sendMessage(MiniMessage.miniMessage().deserialize("<red> Can't connect you to server "+ name));
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        p.sendMessage(MiniMessage.miniMessage().deserialize("<red> Can't connect you to server "+ name));
                        throw new RuntimeException(e);
                    }
                }else{
                    p.sendMessage(MiniMessage.miniMessage().deserialize("<red>You dont have permission to join this server"));
                }
            }else{
                ArrayList<Component> list = new ArrayList<>();
                list.add(MiniMessage.miniMessage().deserialize("<red>Missing server argument.\nList of servers you can join"));
                for(String name : suggest(invocation)){
                    list.add(MiniMessage.miniMessage().deserialize("<green>"+name));
                }
                p.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),list));
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if(invocation.arguments().length > 1) return new ArrayList<>();
        ArrayList<String> allowedServer = getAllowedServer(invocation);
        return allowedServer;
    }

    @NotNull
    public static ArrayList<String> getAllowedServer(Invocation invocation) {
        ArrayList<RegisteredServer> servers = new ArrayList<>(SERVER.getAllServers());
        ArrayList<String> allowedServer = new ArrayList<>();
        for(RegisteredServer server : servers){
            String name = server.getServerInfo().getName();
            if(!serverLocked.has(name)){
                serverLocked.put(name,false);
            }
            if(serverLocked.getBoolean(name)){
                if(invocation.source().hasPermission("serverPermission.server."+ name)){
                    allowedServer.add(name);
                } else{
                    if(serverClusters.keySet().stream().anyMatch(key -> serverClusters.getJSONArray(key).toList().stream().anyMatch(clusteredServer -> {
                        if(clusteredServer instanceof String clusteredServerName){
                            if(clusteredServerName.equals(name)){
                                return invocation.source().hasPermission("serverPermission.serverCluster." + key);
                            }
                        }
                        return false;
                    }))){
                        allowedServer.add(name);
                    }
                }
            }else{
                allowedServer.add(name);
            }
        }
        return allowedServer;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("serverPermission.server");
    }
}
