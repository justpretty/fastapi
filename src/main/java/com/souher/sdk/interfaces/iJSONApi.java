package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.model.Session;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import spark.Request;
import spark.Response;

import java.util.Arrays;

public interface iJSONApi extends iApi
{
    JSONObject getJSON(JSONObject m, int userid) throws Exception;

    default boolean hasPost()
    {
        return true;
    }

    default boolean hasGet()
    {
        return false;
    }

    default String handle(Request request, Response response)
    {
        String a=request.body();
        JSONObject result=new JSONObject();
        String key="";
        try
        {
            JSONObject json = JSONObject.parseObject(a);
            int userid=0;
            if(json.containsKey("_"))
            {
                JSONObject b=json.getJSONObject("_");
                if(b.containsKey("session_key"))
                {
                    key=b.getString("session_key");
                    if(key!=null)
                    {
                        userid= Session.userid(key);
                    }
                }
                json.remove("_");
            }
            if(userid==0&&request.attribute("__from__")!=null&&request.attribute("__from__").equals("outside"))
            {
                result.put("error", "授权信息已过期，请重新登陆！");
                result.put("ip", request.ip());
                iApp.print(iJSONApi.class.getSimpleName(),key+":"+userid+":IP:"+request.ip()+":"+result.toJSONString());
                return JSON.toJSONString(result);
            }
            iApp.print(iJSONApi.class.getSimpleName(),key+":"+userid+":IP:"+request.ip()+"");
            result=this.getJSON(json,userid);
            JSONObject extras=new JSONObject();
            if(result.containsKey("_"))
            {
                extras=result.getJSONObject("_");
            }
            extras.put("session_key",key);
            result.put("_",extras);
            return JSON.toJSONString(result);
        }
        catch (Exception e)
        {
            iApp.error(e);
            JSONObject extras=new JSONObject();
            extras.put("session_key",key);
            result.put("_",extras);
            result.put("error",e.getMessage());
            return JSON.toJSONString(result);
        }
    }
}
