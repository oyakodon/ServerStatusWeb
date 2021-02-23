package com.oykdn.plugins.serverstatusweb;

import com.oykdn.plugins.serverstatusweb.handlers.StaticHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.*;

import net.md_5.bungee.api.plugin.Plugin;

import com.oykdn.plugins.serverstatusweb.handlers.APIHandler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@SuppressWarnings("unused")
public final class ServerStatusWeb extends Plugin
{
    private HttpServer httpServer;

    private Configuration config;

    @Override
    public void onEnable()
    {
        loadConfig();

        startServer(config.getInt("port"));

        getProxy().getScheduler().schedule(this, () ->
                ServerOnlineChecker.getInstance().UpdateOnlineStatuses(getProxy().getServers().values()), 1, 3, TimeUnit.MINUTES);
        ServerOnlineChecker.getInstance().UpdateOnlineStatuses(getProxy().getServers().values());

        getLogger().info("ServerStatusWeb is successfully loaded!");
    }

    @Override
    public void onDisable()
    {
        if (httpServer != null)
        {
            shutdownServer();
        }

        getLogger().info("Bye.");
    }

    private void loadConfig()
    {
        if (!getDataFolder().exists())
        {
            boolean ok = getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists())
        {
            // load default config
            try (InputStream in = getResourceAsStream("config.yml"))
            {
                Files.copy(in, file.toPath());
            } catch (IOException ex)
            {
                getLogger().warning(ex.toString());
            }
        }

        try
        {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ex)
        {
            getLogger().warning(ex.toString());
        }
    }

    private void startServer(int port)
    {
        try
        {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        }
        catch (IOException ex)
        {
            getLogger().warning(ex.getMessage());
        }

        String staticPath = config.getString("static.path");
        boolean useStatic = config.getBoolean("static.enable");

        String apiPath = config.getString("api.path");

        if (useStatic)
        {
            httpServer.createContext(staticPath, new StaticHandler(getDataFolder(), staticPath.substring(0, staticPath.length() - 1)));
        }
        httpServer.createContext(apiPath, new APIHandler(apiPath.substring(0, apiPath.length() - 1)));

        ExecutorService executor = new ThreadPoolExecutor(
                4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        httpServer.setExecutor(executor);

        httpServer.start();

        getLogger().info("ServerStatusWeb is listening on 0.0.0.0:" + port);
    }

    private void shutdownServer()
    {
        httpServer.stop(0);

        Executor executor = httpServer.getExecutor();
        if (executor instanceof ExecutorService)
        {
            ExecutorService service = (ExecutorService) executor;
            service.shutdown();
            try
            {
                if (!service.awaitTermination(5, TimeUnit.SECONDS))
                {
                    service.shutdownNow();
                }
            } catch (InterruptedException ex)
            {
                getLogger().warning(ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
