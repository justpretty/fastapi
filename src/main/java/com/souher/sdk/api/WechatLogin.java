package com.souher.sdk.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.model.Session;
import com.souher.sdk.http.HttpRequest;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.interfaces.iApi;
import com.souher.sdk.interfaces.iWechatUser;
import spark.Request;
import spark.Response;

public class WechatLogin implements iApi
{
    @Override
    public boolean hasPost()
    {
        return true;
    }

    @Override
    public boolean hasGet()
    {
        return false;
    }

    @Override
    public String urlPath()
    {
        return "/wechatlogin";
    }

    @Override
    public String handle(Request request, Response response)
    {
        JSONObject result=new JSONObject();

        try
        {
            JSONObject jsonObject= JSON.parseObject(request.body());
            iApp.debug(jsonObject.toString(),"WechatLogin Input");
            if(!jsonObject.containsKey("_"))
            {
                result.put("error","请求出错！");
                iApp.debug(result.toString(),"WechatLogin Output");
                return JSON.toJSONString(result);
            }
            JSONObject extraObject=jsonObject.getJSONObject("_");
            if(extraObject.containsKey("code"))
            {
                String code=extraObject.getString("code");
                String url="https://api.weixin.qq.com/sns/jscode2session";
                JSONObject jsonResponse=HttpRequest.NewPostRequest()
                        .setUrl(url)
                        .addQueryParameter("appid", iAppConfig.wechatConfig().appid())
                        .addQueryParameter("secret", iAppConfig.wechatConfig().appsecret())
                        .addQueryParameter("js_code",code)
                        .addQueryParameter("grant_type","authorization_code")
                        .send().getJsonResponse();
                String sessionkey=jsonResponse.getString("session_key");
                String openid=jsonResponse.getString("openid");
                iApp.debug(jsonResponse.toJSONString(),"jscode2session");
                iWechatUser user=iAppConfig.newUser().userLoginByOpenid(openid);
                iApp.debug(user.toString(),"user");
                if(!user.checkEnabled())
                {
                    result.put("error","userdisabled");
                    return JSON.toJSONString(result);
                }
                int id=0;
                Object idObject=user.getClass().getDeclaredField("id").get(user);
                if(idObject!=null)
                {
                    id= Integer.parseInt(idObject.toString());
                }
                iApp.debug(sessionkey,"sessionkey");
                Session.save(sessionkey,id,7200);
                JSONObject extra=new JSONObject();
                extra.put("session_key",sessionkey);
                extra.put("user",user.getClass().cast(user));
                result.put("_",extra);
                iApp.debug(result.toString(),"WechatLogin Output");
                return JSON.toJSONString(result);
            }
            else if(extraObject.containsKey("session_key")&&extraObject.containsKey("wechat_user_info"))
            {
                JSONObject wechatUserInfo=extraObject.getJSONObject("wechat_user_info");
                String sessionkey=extraObject.getString("session_key");
                if(wechatUserInfo.containsKey("avatarUrl"))
                {
                    int userid= Session.userid(sessionkey);
                    if(userid==0)
                    {
                        result.put("error", "授权信息已过期，请重新登陆！");
                        iApp.debug(result.toString(),"WechatLogin Output");
                        return JSON.toJSONString(result);
                    }
                    iWechatUser user=iAppConfig.newUser().userSaveInfo(userid,
                            wechatUserInfo.getString("nickName"),
                            wechatUserInfo.getString("avatarUrl"),
                            wechatUserInfo.getInteger("gender"),
                            wechatUserInfo.getString("country"),
                            wechatUserInfo.getString("province"),
                            wechatUserInfo.getString("city"),
                            wechatUserInfo.getString("language")
                            );
                    if(user==null)
                    {
                        result.put("error", "授权信息已过期，请重新登陆！");
                        iApp.debug(result.toString(),"WechatLogin Output");
                        return JSON.toJSONString(result);
                    }
                    if(!user.checkEnabled())
                    {
                        result.put("error","userdisabled");
                        return JSON.toJSONString(result);
                    }
                    iApp.debug(JSON.toJSONString(user));
                    JSONObject extra=new JSONObject();
                    extra.put("session_key",sessionkey);
                    extra.put("user",(DataModel)user);
                    result.put("_",extra);
                    iApp.debug(result.toString(),"WechatLogin Output");
                    return JSON.toJSONString(result);
                }
            }

            result.put("error","未登陆成功！");
            iApp.error("未登陆成功！");
            iApp.debug(result.toString(),"WechatLogin Output");
            return JSON.toJSONString(result);
        }
        catch (Exception e)
        {
            result.put("error",e.getMessage());
            iApp.error(e);
            iApp.debug(result.toString(),"WechatLogin Output");
            return JSON.toJSONString(result);
        }
    }
}
