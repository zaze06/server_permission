package me.zacharias.serverpermission.commands;

import com.velocitypowered.api.command.CommandSource;
import me.zacharias.serverpermission.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.zacharias.serverpermission.ServerPermission.logger;
import static me.zacharias.serverpermission.ServerPermission.serverClusters;

public class Cluster implements Command {
    @Override
    public String getCommand() {
        return "cluster";
    }

    @Override
    public void execute(Invocation invocation) {
        String[] arguments = invocation.arguments();
        CommandSource sender = invocation.source();
        boolean valid = true;
        ArrayList<Component> list = new ArrayList<>();
        if(arguments.length > 0) {
            if (arguments[0].equalsIgnoreCase("list")           && invocation.source().hasPermission("serverPermission.cluster.list")){
                if(arguments.length > 1) {
                    list.add(MiniMessage.miniMessage().deserialize("<green>List of servers under cluster " + arguments[1]));
                    for (Object obj : serverClusters.getJSONArray(arguments[1])) {
                        if (obj instanceof String cluster) {
                            list.add(MiniMessage.miniMessage().deserialize("<gold>- <reset>"+cluster));
                        }
                    }
                }else{
                    list.add(MiniMessage.miniMessage().deserialize("<aqua>List of clusters"));
                    for(String key : serverClusters.keySet()){
                        list.add(MiniMessage.miniMessage().deserialize("<gold>- <green>"+key));
                    }
                }
            }
            else if (arguments[0].equalsIgnoreCase("create")    && invocation.source().hasPermission("serverPermission.cluster.create")){
                if(arguments.length > 2){
                    String clusterName = arguments[1];
                    JSONArray servers = new JSONArray();

                    list.add(MiniMessage.miniMessage().deserialize("<aqua> Adding servers to cluster "+clusterName));
                    for(int i = 2; i < arguments.length; i++){
                        servers.put(arguments[i]);
                        list.add(MiniMessage.miniMessage().deserialize("<gold>- <green> added "+arguments[i]+" to cluster"));
                    }
                    serverClusters.put(clusterName, servers);
                }else{
                    valid = false;
                }
            }
            else if (arguments[0].equalsIgnoreCase("remove")    && invocation.source().hasPermission("serverPermission.cluster.remove")){
                if(arguments.length > 2){
                    String clusterName = arguments[1];
                    ArrayList<String> servers = new ArrayList<>();
                    for(int i = 2; i < arguments.length; i++){
                        servers.add(arguments[i]);
                    }

                    if(serverClusters.has(clusterName)) {
                        JSONArray cluster = serverClusters.getJSONArray(clusterName);

                        ArrayList<Object> objects = new ArrayList<>(cluster.toList());
                        list.add(MiniMessage.miniMessage().deserialize("<aqua>Removing servers from cluster "+clusterName));
                        for(String server : servers){
                            if(!objects.remove(server)){
                                list.add(MiniMessage.miniMessage().deserialize("<red>Server "+server+" doesn't exist"));
                                continue;
                            }
                            list.add(MiniMessage.miniMessage().deserialize("<gold>- <green>Server "+server+" have ben removed"));
                        }
                        cluster = new JSONArray();

                        objects.forEach(cluster::put);

                        serverClusters.put(clusterName, objects);
                    }else{
                        valid = false;
                    }
                }
            }
            else if (arguments[0].equalsIgnoreCase("add")       && invocation.source().hasPermission("serverPermission.cluster.add")){
                if(arguments.length > 2){
                    String clusterName = arguments[1];
                    list.add(MiniMessage.miniMessage().deserialize("<aqua>Adding servers to cluster "+clusterName));
                    for(int i = 2; i < arguments.length; i++){
                        serverClusters.getJSONArray(clusterName).put(arguments[i]);
                        list.add(MiniMessage.miniMessage().deserialize("<gold>- <reset>"+arguments[i]));
                    }
                }
            }
            else if (arguments[0].equalsIgnoreCase("delete")    && invocation.source().hasPermission("serverPermission.cluster.delete")){
                if(arguments.length > 1){
                    if(serverClusters.remove(arguments[1]) == null){
                        list.add(MiniMessage.miniMessage().deserialize("<red> cluster "+arguments[1]+" dose not exist"));
                        valid = false;
                    }else {
                        list.add(MiniMessage.miniMessage().deserialize("<green> deleted cluster "+arguments[1]));
                    }
                }
            }
        }else{
            valid = false;
        }

        if(valid){
            sender.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),list));
        }else{
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>failed to run command"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        ArrayList<String> list = new ArrayList<>();
        String[] arguments = invocation.arguments();
        if(arguments.length < 2) {
            if      (invocation.source().hasPermission("serverPermission.cluster.list"))    list.add("list");
            if (invocation.source().hasPermission("serverPermission.cluster.create"))  list.add("create");
            if (invocation.source().hasPermission("serverPermission.cluster.remove"))  list.add("remove");
            if (invocation.source().hasPermission("serverPermission.cluster.add"))     list.add("add");
            if (invocation.source().hasPermission("serverPermission.cluster.delete"))  list.add("delete");
        }
        else if (arguments[0].equalsIgnoreCase("list") && invocation.source().hasPermission("serverPermission.cluster.list")) {
            if(arguments.length > 2) return list;
            list.addAll(serverClusters.keySet());
        }
        else if (arguments[0].equalsIgnoreCase("create") && invocation.source().hasPermission("serverPermission.cluster.create")) {
            if(arguments.length > 2){
                list.addAll(Server.getAllowedServer(invocation));
                ArrayList<String> servers = new ArrayList<>(Arrays.stream(arguments).toList());
                servers.remove(0);
                servers.remove(0);
                list.removeAll(servers);
            }else {
                list.add("<cluster name>");
            }
        }
        else if (arguments[0].equalsIgnoreCase("remove") && invocation.source().hasPermission("serverPermission.cluster.remove")) {
            if(serverClusters.has(arguments[1])){
                serverClusters.getJSONArray(arguments[1]).toList().forEach(obj -> {
                    if(obj instanceof String str){
                        ArrayList<String> servers = new ArrayList<>(Arrays.stream(arguments).toList());
                        servers.remove(0);
                        servers.remove(0);
                        if(!servers.contains(str)) list.add(str);
                    }
                });
            }
            list.addAll(serverClusters.keySet());
        }
        else if (arguments[0].equalsIgnoreCase("add") && invocation.source().hasPermission("serverPermission.cluster.add")) {
            if(serverClusters.has(arguments[1])){
                serverClusters.getJSONArray(arguments[1]).toList().forEach(obj -> {
                    if(obj instanceof String str){
                        ArrayList<String> servers = new ArrayList<>(Arrays.stream(arguments).toList());
                        servers.remove(0);
                        servers.remove(0);
                        servers.removeAll(serverClusters.getJSONArray(arguments[1]).toList().stream().toList());
                        if(!servers.contains(str)) list.add(str);
                    }
                });
            }
            list.addAll(serverClusters.keySet());
        }
        else if (arguments[0].equalsIgnoreCase("delete") && invocation.source().hasPermission("serverPermission.cluster.delete")) {
            list.addAll(serverClusters.keySet());
        }
        return list;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("serverPermission.cluster");
    }
}
