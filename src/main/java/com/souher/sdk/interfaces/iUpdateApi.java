package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import spark.Session;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public interface iUpdateApi extends iJSONApi
{
    default String urlPath()
    {
        return "/api/update";
    }

    default JSONObject getJSON(JSONObject m,int userid) throws Exception
    {
        iApp.debug("input:iUpdateApi:"+userid+"\n"+ JSON.toJSONString(m,true));

        JSONObject map=new JSONObject();
        DataResult<DataModel> dataResult=iApiUpdatableModel.parseJSON(m);

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
            boolean hasId=a.hasId();
            iApp.debug("filterUpdating");
            ((iApiUpdatableModel) a).filterUpdate();
            if(!hasId )
            {
                iApp.debug("filterUpdating:"+hasId);
                Object id=a.getClass().getDeclaredField("id").get(a);
                if(id!=null)
                {
                    iApp.debug("filterUpdating:"+id);
                    map.put(apiTagName,(int)id);
                    return;
                }
            }
            int num=a.save();
            iApp.debug(a.toString(),"savedModel");
            map.put(apiTagName,num);
        });
        iApp.debug("output:iUpdateApi:"+userid+"\n"+ JSON.toJSONString(map,true));
        return map;
    }

}
