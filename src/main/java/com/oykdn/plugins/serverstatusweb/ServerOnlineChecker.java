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

    private final HashMap<String, Boolean> serversOnlineStatuses;

    private ServerOnlineChecker()
    {
        serversOnlineStatuses = new HashMap<>();
    }

    public void UpdateOnlineStatuses(Collection<ServerInfo> servers)
    {
        for(ServerInfo info : servers)
        {
            info.ping((result, error) -> serversOnlineStatuses.put(info.getName(), (error == null)));
        }
    }

    public boolean getServerOnline(String name)
    {
        if (!serversOnlineStatuses.containsKey(name)) return false;

        return serversOnlineStatuses.get(name);
    }
}
