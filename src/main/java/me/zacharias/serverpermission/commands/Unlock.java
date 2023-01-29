package me.zacharias.serverpermission.commands;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.zacharias.serverpermission.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

import static me.zacharias.serverpermission.ServerPermission.SERVER;
import static me.zacharias.serverpermission.ServerPermission.serverLocked;

public class Unlock implements Command {
    @Override
    public String getCommand() {
        return "unlock";
    }

    @Override
    public void execute(Invocation invocation) {
        ArrayList<Component> list = new ArrayList<>();
        list.add(MiniMessage.miniMessage().deserialize("<blue>Unlocking servers"));
        for(String name : invocation.arguments()){
            if(SERVER.getServer(name).isPresent()){
                if(serverLocked.getBoolean(name)){
                    serverLocked.put(name, false);
                    list.add(MiniMessage.miniMessage().deserialize("<click:copy_to_clipboard:serverPermission.server."+name+"><hover:show_text:'<aqua>Click to copy permission node'>" +
                            "<green>Server "+name+" is now unlocked. Players no longer needs permission: serverPermission.server."+name+" to join the server</click>"));
                }else{
                    list.add(MiniMessage.miniMessage().deserialize("<click:copy_to_clipboard:serverPermission.server."+name+"><hover:show_text:'<aqua>Click to copy permission node'>" +
                            "<red>Server "+name+" is already unlocked</click>"));
                }
            }else{
                list.add(MiniMessage.miniMessage().deserialize("<red>Server "+name+" doesn't exist"));
            }
        }
        invocation.source().sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),list));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        ArrayList<RegisteredServer> servers = new ArrayList<>(SERVER.getAllServers());
        ArrayList<String> allowedServer = new ArrayList<>();
        for(RegisteredServer server : servers){
            String name = server.getServerInfo().getName();
            if(serverLocked.getBoolean(name)){
                if(invocation.source().hasPermission("serverPermission.server."+ name)){
                    allowedServer.add(name);
                }
            }
        }
        return allowedServer;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("serverPermission.unlock");
    }
}
