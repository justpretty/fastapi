package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.business.CacheRequestWorker;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.database.DatabaseOptions;
import com.souher.sdk.extend.ExtendThread;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;

import java.util.ArrayList;

public interface iSelectApi extends iJSONApi
{
    default String urlPath()
    {
        return "/api/select";
    }

    default boolean forceGet()
    {
        return false;
    }

    default JSONObject getJSON(JSONObject m, int userid) throws Exception
    {
        iApp.debug("iSelectApi.getJSON.input:\n"+JSON.toJSONString(m,true));
        JSONObject n=JSONObject.parseObject(m.toJSONString());
        JSONObject map=new JSONObject();
        DataResult<DataModel> dataResult=iApiSelectableModel.parseJSON(m,userid);
        dataResult.forEachForcely(a->{
            String apiTagName=a.getClass().getSimpleName().toLowerCase();
            ((iApiSelectableModel) a).filterSelect(userid);

            JSONObject obj=n.getJSONObject(apiTagName);
            if(obj.containsKey("_")&&obj.getJSONObject("_").containsKey("noncache"))
            {
                obj.getJSONObject("_").remove("noncache");
            }
            JSONObject tempObject=new JSONObject();
            tempObject.put(apiTagName,obj);
            String request=tempObject.toJSONString();

            if(!forceGet()&&a.noncache==null)
            {
                JSON cache= CacheRequestWorker.get(request,a);
                if(cache!=null)
                {
                    map.put(apiTagName,cache);
                    iApp.debug("iSelectApi.getJSON.usingcache:"+apiTagName);
                    return;
                }
            }
            String[] orders=((iApiSelectableModel) a).orderFields();
            String orderFieldName="";
            for (int i = 0; i < orders.length; i++)
            {
                String mainstr=orders[i];
                String mainstrs[]=mainstr.split("\\s+|,");
                for (int i1 = 0; i1 < mainstrs.length; i1++)
                {
                    String str=mainstrs[i1];
                    if(str.trim().isEmpty())
                    {
                        continue;
                    }
                    if(orderFieldName.isEmpty())
                    {
                        orderFieldName=str;
                        continue;
                    }
                    if(str.equals("asc"))
                    {
                        a.orderAscBy(orderFieldName);
                        orderFieldName="";
                    }
                    else if(str.equals("desc"))
                    {
                        a.orderDescBy(orderFieldName);
                        orderFieldName="";
                    }
                    else
                    {
                        a.orderAscBy(orderFieldName);
                        orderFieldName=str;
                    }
                }
            }
            if(!orderFieldName.isEmpty())
            {
                a.orderAscBy(orderFieldName);
            }
            ArrayList<String> list=a.option().get(DatabaseOptions.limit);
            if(list.get(list.size()-1).equals("1"))
            {
                DataModel dataModel=JSONObject.parseObject(JSONObject.toJSONString(a),a.getClass());
                a.first();
                map.put(apiTagName,a);
                    new ExtendThread(()->{
                        CacheRequestWorker.save(request,dataModel,a);
                    }).start();
                return;
            }
            if(!a.option().containsKey(DatabaseOptions.limit))
            {
                a.option().append(DatabaseOptions.limit,String.valueOf(((iApiSelectableModel) a).pageSize()));
            }

            DataResult<? extends DataModel> result=a.selectSimilarModels(a.getClass());
            map.put(apiTagName,result);
            iApp.debug("iSelectApi.getJSON.savecache:"+apiTagName+":"+result.toString()+" @@@ idData:"+JSON.toJSONString(result.idListData()));
            //if(iAppConfig.webConfig().apiVersion().isEmpty())
            //{
            new ExtendThread(()->{
                CacheRequestWorker.save(request,a,result);
            }).start();
            //}
        });
        iApp.debug("iSelectApi.getJSON.output:"+JSON.toJSONString(map));
        return map;
    }


}
