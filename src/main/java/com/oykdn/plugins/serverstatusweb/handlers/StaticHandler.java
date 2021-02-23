package com.oykdn.plugins.serverstatusweb.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticHandler implements HttpHandler
{
    private final String baseUrl;
    private final Logger logger;
    private final File baseFolder;

    private final static String IndexFile = "index.html";

    public StaticHandler(File dataFolder, String _baseUrl)
    {
        baseUrl = _baseUrl;
        logger = ProxyServer.getInstance().getLogger();
        baseFolder = new File(dataFolder, "static");

        if (!baseFolder.exists())
        {
            @SuppressWarnings("unused")
            boolean ok = baseFolder.mkdir();
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String path = httpExchange.getRequestURI().toString();

        logger.info("REQ (static): " + path);

        HttpResponse res = null;

        path = path.replace(baseUrl, "");

        if (path.equals("/"))
        {
            path = IndexFile;
        }

        File file = new File(baseFolder, path);

        if (file.exists() && file.canRead())
        {
            res = new HttpResponse();

            res.ContentType = detectType(path);
            res.StatusCode = 200;

            res.ResponseBody = Files.readAllBytes(file.toPath());
        }

        if (res == null)
        {
            res = new HttpResponse("404 Not Found", 404);
        }

        Headers resHeaders = httpExchange.getResponseHeaders();
        resHeaders.set("Content-Type", res.ContentType);
        resHeaders.set("Last-Modified", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));

        httpExchange.sendResponseHeaders(res.StatusCode, res.ResponseBody.length);

        OutputStream os = httpExchange.getResponseBody();
        os.write(res.ResponseBody);
        os.close();
    }

    private String detectType(String fileName)
    {
        String type = "application/octet-stream";

        Matcher m = Pattern.compile("\\.(?<ext>.*?)$", Pattern.CASE_INSENSITIVE).matcher(fileName);
        if (m.find())
        {
            switch (m.group("ext"))
            {
                /* TEXT */
                case "html":
                case "htm":
                    type = "text/html";
                    break;
                case "json":
                    type = "application/json";
                    break;
                case "css":
                    type = "text/css";
                    break;
                /* BINARY */
                case "png":
                    type = "image/png";
                    break;
                case "jpeg":
                case "jpg":
                    type = "image/jpeg";
                    break;
                default:
                    break;
            }
        }

        return type;
    }
}
