package com.oykdn.plugins.serverstatusweb.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.md_5.bungee.api.ProxyServer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MojangSkinAPI
{
    private static final String UserIDURLBase = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String UserProfileURLBase = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static byte[] GetIcon(String uuid)
    {
        String textureUrl = GetSkinURL(uuid);

        if (textureUrl == null)
        {
            return null;
        }

        BufferedImage iconImage = new BufferedImage(8,8, BufferedImage.TYPE_INT_RGB);

        try
        {
            BufferedImage skinImage = ImageIO.read(new URL(textureUrl));

            for(int x = 0; x < 8; x++)
            {
                for (int y = 0; y < 8; y++)
                {
                    iconImage.setRGB(x, y, skinImage.getRGB(x + 8, y + 8));
                }
            }

        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().warning(ex.toString());
            return null;
        }

        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedOutputStream os = new BufferedOutputStream( bos );

            iconImage.flush();
            ImageIO.write(iconImage, "png", os);

            return bos.toByteArray();

        }  catch(IOException ex)
        {
            ProxyServer.getInstance().getLogger().warning(ex.toString());
            return null;
        }
    }

    public static String GetSkinURL(String uuid)
    {
        String json = HttpRestClient.Get(UserProfileURLBase + uuid.replace("-", ""));
        String textureProp;

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            textureProp = root.get("properties").get(0).get("value").asText();
        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().warning(ex.toString());
            return null;
        }

        json = new String(Base64.getDecoder().decode(textureProp), StandardCharsets.UTF_8);
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.get("textures").get("SKIN").get("url").asText();
        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().warning(ex.toString());
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static String GetUUID(String userName)
    {
        String uuid = null;
        String json = HttpRestClient.Get(UserIDURLBase + userName);

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            uuid = root.get("id").asText();
        } catch (IOException ex)
        {
            ProxyServer.getInstance().getLogger().warning(ex.toString());
        }

        return uuid;
    }
}
