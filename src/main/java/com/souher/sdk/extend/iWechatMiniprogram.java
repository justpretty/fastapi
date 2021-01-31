package com.souher.sdk.extend;

import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.http.HttpRequest;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;

import java.io.File;

public interface iWechatMiniprogram
{

    static String getToken() throws Exception
    {
        String appid= iAppConfig.wechatConfig().appid();
        String secret=iAppConfig.wechatConfig().appsecret();
        JSONObject jsonObject=HttpRequest.NewGetRequest().setUrl("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential")
                .addQueryParameter("appid",appid)
                .addQueryParameter("secret",secret)
                .send(5000)
                .getJsonResponse();
        if(jsonObject.containsKey("access_token"))
        {
            return jsonObject.getString("access_token");
        }
        String error="error in token response:"+jsonObject.toString();
        if(jsonObject.containsKey("errmsg"))
        {
            error=jsonObject.getString("errmsg");
        }
        throw new Exception(error);
    }

}
