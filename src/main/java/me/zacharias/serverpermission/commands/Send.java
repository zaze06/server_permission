package me.zacharias.serverpermission.commands;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.zacharias.serverpermission.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.zacharias.serverpermission.ServerPermission.SERVER;
import static me.zacharias.serverpermission.ServerPermission.serverLocked;

public class Send implements Command {
    @Override
    public String getCommand() {
        return "send";
    }

    @Override
    public void execute(Invocation invocation) {

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        ArrayList<String> list = new ArrayList<>();
        if(invocation.arguments().length > 1){
            Optional<Player> optionalPlayer = SERVER.getPlayer(invocation.arguments()[0]);
            if(optionalPlayer.isEmpty()){
                list.add("Player in first argument doesn't exist");
                return list;
            }
            Player p = optionalPlayer.get();
            ArrayList<RegisteredServer> servers = new ArrayList<>(SERVER.getAllServers());
            for(RegisteredServer server : servers){
                String name = server.getServerInfo().getName();
                boolean canPlayerJoinServer = false;
                if(invocation.source().hasPermission("serverPermission.send.ignoreTargetPermission")){
                    canPlayerJoinServer = true;
                }else{
                    canPlayerJoinServer = p.hasPermission("serverPermission.server."+ name);
                }
                if(serverLocked.getBoolean(name)){
                    if(invocation.source().hasPermission("serverPermission.server."+ name) && canPlayerJoinServer){
                        list.add(name);
                    }
                }else{
                    list.add(name);
                }
            }
        }else{
            for(Player p : SERVER.getAllPlayers()){
                list.add(p.getUsername());
            }
        }
        return list;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("serverPermission.send");
    }
}
