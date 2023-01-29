package me.zacharias.serverpermission;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;

public interface Command extends SimpleCommand {
    String getCommand();
    default String[] getAllies(){
        return new String[0];
    }
}
