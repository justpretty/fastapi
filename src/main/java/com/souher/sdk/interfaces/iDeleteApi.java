package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import spark.Session;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public interface iDeleteApi extends iJSONApi
{
    default String urlPath()
    {
        return "/api/delete";
    }

    default JSONObject getJSON(JSONObject m, int userid) throws Exception
    {
        iApp.debug("input:iDeleteApi:"+userid+"\n"+ JSON.toJSONString(m,true));

        JSONObject map=new JSONObject();
        DataResult<? extends DataModel> dataResult=iApiDeletableModel.parseJSON(m);
        dataResult.forEachForcely(a->{
            String apiTagName=a.getClass().getSimpleName().toLowerCase();
            boolean hasFansField=false;
            boolean hasEqualedFansField=false;
            for(Field field : a.getClass().getDeclaredFields())
            {
                if(field.getName().equals("fans_id")
                        ||field.getName().endsWith("_fans_id"))
                {
                    hasFansField=true;
                    Object cc=field.get(a);
                    if(cc!=null&&cc.equals(userid))
                    {
                        hasEqualedFansField=true;
                        break;
                    }
                }
            }
            if(hasFansField && !hasEqualedFansField)
            {
                throw new Exception("未登陆或操作未授权！");
            }
            ((iApiDeletableModel) a).filterDelete();
            map.put(apiTagName,a.delete());

        });
        iApp.debug("output:iDeleteApi:"+userid+"\n"+ JSON.toJSONString(map,true));
        return map;
    }
}
