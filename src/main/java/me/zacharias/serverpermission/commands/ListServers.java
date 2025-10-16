package me.zacharias.serverpermission.commands;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.zacharias.serverpermission.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.zacharias.serverpermission.ServerPermission.SERVER;

public class ListServers implements Command {

    @Override
    public String getCommand() {
        return "Servers";
    }

    @Override
    public String[] getAllies() {
        return new String[] { "Servers", "ListServers", "ls"};
    }

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player p){
            ArrayList<Component> list = new ArrayList<>();
            for(RegisteredServer server : SERVER.getAllServers()){
                if(p.hasPermission("serverPermission.server."+server.getServerInfo().getName()))
                {
                    AtomicBoolean serverOnline = new AtomicBoolean(false);
                    try {
                        server.ping().thenAcceptAsync(ping -> {
                            serverOnline.set(true);}).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    list.add(MiniMessage.miniMessage().deserialize("<blue>"+server.getServerInfo().getName()+" "+(serverOnline.get()?"<green>online":"<red>offline")));
                }
            }
            p.sendMessage(Component.join(JoinConfiguration.newlines(),list));
        }
    }
}
