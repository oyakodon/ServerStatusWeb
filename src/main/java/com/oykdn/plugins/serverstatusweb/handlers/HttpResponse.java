package com.oykdn.plugins.serverstatusweb.handlers;

import java.nio.charset.StandardCharsets;

public class HttpResponse
{
    public HttpResponse()
    {
        ResponseBody = null;
        StatusCode = 200;
    }

    public HttpResponse(String text)
    {
        ContentType = "text/html";
        ResponseBody = text.getBytes(StandardCharsets.UTF_8);
        StatusCode = 200;
    }

    public HttpResponse(String text, int code)
    {
        ContentType = "text/html";
        ResponseBody = text.getBytes(StandardCharsets.UTF_8);
        StatusCode = code;
    }

    public HttpResponse(String text, String type)
    {
        ContentType = type;
        ResponseBody = text.getBytes(StandardCharsets.UTF_8);
        StatusCode = 200;
    }

    public byte[] ResponseBody;
    public String ContentType;
    public int StatusCode;
}