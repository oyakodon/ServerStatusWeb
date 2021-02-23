package com.oykdn.plugins.serverstatusweb.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oykdn.plugins.serverstatusweb.ServerOnlineChecker;
import com.oykdn.plugins.serverstatusweb.api.MojangSkinAPI;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class APIHandler implements HttpHandler
{
    private final String baseUrl;

    private final Logger logger;

    private final HashMap<String, byte[]> icon_cache = new HashMap<>();
    private final HashMap<String, Function<String, HttpResponse>> routes = new HashMap<>();

    public APIHandler(String _baseUrl)
    {
        baseUrl = _baseUrl;
        logger = ProxyServer.getInstance().getLogger();

        registerRoutes();
    }

    private void registerRoutes()
    {
        routes.put("/", (path) -> new HttpResponse("ServerStatusWeb API"));
        routes.put("/servers", this::getServersInfo);
        routes.put("/icons/(.*)", this::getIcon);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String path = httpExchange.getRequestURI().toString();

        logger.info("REQ (API): " + path);

        path = path.replace(baseUrl, "");

        HttpResponse res = null;
        for(String pattern : routes.keySet())
        {
            if (path.matches(pattern))
            {
                res = routes.get(pattern).apply(path);
                break;
            }
        }

        if (res == null)
        {
            res = new HttpResponse("404 Not Found", 404);
        }

        Headers resHeaders = httpExchange.getResponseHeaders();
        resHeaders.set("Content-Type", res.ContentType);
        resHeaders.set("Last-Modified", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        resHeaders.add("Access-Control-Allow-Headers","x-prototype-version,x-requested-with");
        resHeaders.add("Access-Control-Allow-Methods","GET");
        resHeaders.add("Access-Control-Allow-Origin","*");

        httpExchange.sendResponseHeaders(res.StatusCode, res.ResponseBody.length);

        OutputStream os = httpExchange.getResponseBody();
        os.write(res.ResponseBody);
        os.close();
    }

    private HttpResponse getServersInfo(String path)
    {
        ServerStatuses status = new ServerStatuses();
        status.servers = new ArrayList<>();

        for (ServerInfo info : ProxyServer.getInstance().getServers().values())
        {
            ServerStatus s = new ServerStatus();
            ServerOnlineChecker.OnlineState onlineState = ServerOnlineChecker.getInstance().getServerOnline(info.getName());

            String version = "";
            if (!onlineState.version.isEmpty())
            {
                Matcher m = Pattern.compile("(\\d+\\.\\d+\\.\\d+)").matcher(onlineState.version);
                if (m.find() && !m.group().isEmpty())
                {
                    version = m.group();
                }
            }

            s.name = info.getName();
            s.playerCount = info.getPlayers().size();
            s.players = info.getPlayers().stream().map(p -> new Player(p.getName())).collect(Collectors.toList());
            s.online = onlineState.isOnline;
            s.version = version;
            s.description = info.getMotd();

            status.servers.add(s);
        }

        ObjectMapper mapper = new ObjectMapper();

        try
        {
            return new HttpResponse(mapper.writeValueAsString(status), "application/json");
        } catch(JsonProcessingException ex)
        {
            logger.warning(ex.toString());
            return new HttpResponse("500 - Internal Server Error", 500);
        }
    }

    private HttpResponse getIcon(String path)
    {
        HttpResponse res = new HttpResponse();

        Pattern p = Pattern.compile("/icons/(?<name>.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(path);
        if (!m.matches() || m.group("name").isEmpty())
        {
            return new HttpResponse("404 Not Found", 404);
        }

        String uuid = MojangSkinAPI.GetUUID(m.group("name"));
        if (uuid == null)
        {
            return new HttpResponse("404 Not Found", 404);
        }

        byte[] icon;
        if (icon_cache.containsKey(uuid))
        {
            icon = icon_cache.get(uuid);
        } else
        {
            icon = MojangSkinAPI.GetIcon(uuid);
            icon_cache.put(uuid, icon);
        }

        if (icon == null)
        {
            return new HttpResponse("404 Not Found", 404);
        }

        res.ResponseBody = icon;
        res.ContentType = "image/png";

        return res;
    }

    private static class ServerStatuses
    {
        public List<ServerStatus> servers;
    }

    private static class ServerStatus
    {
        public String name;
        public int playerCount;
        public List<Player> players;
        public boolean online;
        public String version;
        public String description;
    }

    private static class Player
    {
        public Player(String _id)
        {
            id = _id;
        }

        public String id;
    }
}
