package com.souher.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.http.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public interface iHttp
{
    static void downloadFile(String urlString,String floder,String filename) throws Exception
    {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(10 * 1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf = new File(floder);
        if (!sf.exists())
        {
            sf.mkdirs();
        }

        OutputStream os = new FileOutputStream(sf.getPath() + "/" + filename);
        // 开始读取
        while ((len = is.read(bs)) != -1)
        {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }
    static String postJson(String url, JSONObject requestBody) throws Exception
    {
        HttpRequest request = HttpRequest.NewPostRequest()
                .setUrl(url)
                .setJsonParameter(
                        requestBody
                );
        request.send();

        String response=request.getStringResponse();

        return response;
    }


    static String get(String url) throws Exception
    {

        HttpRequest request = HttpRequest.NewGetRequest()
                .setUrl(url);

        request.send();
        String response=request.getStringResponse();

        return response;
    }

}
