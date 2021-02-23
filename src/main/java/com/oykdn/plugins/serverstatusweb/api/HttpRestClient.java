package com.oykdn.plugins.serverstatusweb.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpRestClient
{
    public static String Get(String url)
    {
        String response = null;

        try
        {
            URL _url = new URL(url);

            HttpURLConnection connection = null;

            try
            {
                connection = (HttpURLConnection) _url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    try (
                        InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                        BufferedReader reader = new BufferedReader(isr)
                    )
                    {
                        response = reader.lines().collect(Collectors.joining("\r\n"));
                    }
                }
            } finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return response;
    }
}
