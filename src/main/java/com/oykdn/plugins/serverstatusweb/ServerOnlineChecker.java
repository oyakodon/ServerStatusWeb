package com.oykdn.plugins.serverstatusweb;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.HashMap;

public class ServerOnlineChecker
{
    private static final ServerOnlineChecker instance = new ServerOnlineChecker();

    public static ServerOnlineChecker getInstance()
    {
        return instance;
    }

    private final HashMap<String, OnlineState> serversOnlineStatuses;

    private ServerOnlineChecker()
    {
        serversOnlineStatuses = new HashMap<>();
    }

    public void UpdateOnlineStatuses(Collection<ServerInfo> servers)
    {
        for(ServerInfo info : servers)
        {
            info.ping((result, error) ->
            {
                boolean isOnline = (error == null);
                serversOnlineStatuses.put(info.getName(), new OnlineState(isOnline, isOnline ? result.getVersion().getName() : ""));
            });
        }
    }

    public OnlineState getServerOnline(String name)
    {
        if (!serversOnlineStatuses.containsKey(name)) return null;

        return serversOnlineStatuses.get(name);
    }

    public static class OnlineState
    {
        public OnlineState(boolean online, String ver)
        {
            isOnline = online;
            version = ver;
        }

        public boolean isOnline;
        public String version;
    }
}
