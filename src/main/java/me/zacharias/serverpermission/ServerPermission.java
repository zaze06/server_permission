package me.zacharias.serverpermission;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.zacharias.serverpermission.commands.*;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;

@Plugin(
        id = "server_permission",
        name = "ServerPermission",
        version = BuildConstants.VERSION,
        description = "A simple plugin that requires you to have permision to join locked servers",
        authors = {"Zacharias"}
)
public class ServerPermission {

    public static ProxyServer SERVER;
    public static Logger logger;
    public static Path dataDirectory;
    public static JSONObject serverLocked = new JSONObject();
    public static JSONObject serverClusters = new JSONObject();

    @Inject
    public ServerPermission(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory){
        ServerPermission.logger = logger;
        ServerPermission.SERVER = server;
        ServerPermission.dataDirectory = dataDirectory;

        File configFile = new File(dataDirectory.toFile(), "lockedServers.json");
        if(configFile.exists()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(configFile));
                String tmp = "";
                StringBuilder tmpData = new StringBuilder();
                while((tmp = in.readLine()) != null){
                    tmpData.append(tmp);
                }
                serverLocked = new JSONObject(tmpData.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            for(RegisteredServer server1 : server.getAllServers()){
                serverLocked.put(server1.getServerInfo().getName(), false);
            }
        }

        File clusterFile = new File(dataDirectory.toFile(), "serverCluster.json");
        if(clusterFile.exists()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(clusterFile));
                String tmp = "";
                StringBuilder tmpData = new StringBuilder();
                while((tmp = in.readLine()) != null){
                    tmpData.append(tmp);
                }
                serverClusters = new JSONObject(tmpData.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommand(new Send());
        registerCommand(new Server());
        registerCommand(new Lock());
        registerCommand(new Unlock());
        registerCommand(new Cluster());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event){
        if(!dataDirectory.toFile().exists()){
            dataDirectory.toFile().mkdir();
        }

        File configFile = new File(dataDirectory.toFile(), "lockedServers.json");
        files(configFile, serverLocked);

        File clusterFile = new File(dataDirectory.toFile(), "serverCluster.json");
        files(clusterFile, serverClusters);
    }

    private void files(File file, JSONObject json) {
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(json.toString(4));
            bufferedWriter.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void registerCommand(Command command) {
        CommandManager commandManager = ServerPermission.SERVER.getCommandManager();
        commandManager.register(commandManager.metaBuilder(command.getCommand()).plugin(this).aliases(command.getAllies()).build(), command);
    }
}
