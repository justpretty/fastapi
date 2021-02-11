package com.souher.sdk.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.api.SelectApi;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.database.DatabaseOptions;
import com.souher.sdk.extend.*;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CacheRequestWorker implements iOnDataInserted,iOnDataUpdated,iOnDataDeleted, iAllModelWatcher
{
    static KeySetMapRedis ids=new KeySetMapRedis("@@IDS");
    static KeySetMapRedis properties=new KeySetMapRedis("@@PROS");
    static KeyRedis cacheOne=new KeyRedis("@@ONE");
    static KeyRedis cacheList=new KeyRedis("@@LIST");
    public static CacheRequestWorker Current=new CacheRequestWorker();

    public static void initAsync()
    {
        new ExtendThread(()->{
            cacheOne.keys2("*").forEach((a,b)->{
                try
                {
                    String request=a.replaceAll("@=",":");
                    iApp.debug("forceSelectApi.request",request);
                    SelectApi.forceSelectApi.getJSON(JSONObject.parseObject(request),0);
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
            cacheList.keys2("*").forEach((a,b)->{
                try
                {
                    String request=a.replaceAll("@=",":");
                    iApp.debug("forceSelectApi.request",request);
                    SelectApi.forceSelectApi.getJSON(JSONObject.parseObject(request),0);
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        }).start();
    }

    static String propertyKey(DataModel model)
    {
        return model.getClass().getSimpleName()+":"+model.toStringWithOutNull().replaceAll(":","@=");
    }
    static String propertyKey(Class<? extends DataModel> cls,String json)
    {
        return cls.getSimpleName()+":"+json;
    }

    static String idKey(Class<? extends DataModel> cls,Object idValue)
    {
        return cls.getSimpleName()+":"+idValue;
    }

    static String cacheKey(Class<? extends DataModel> cls,String request)
    {
        return cls.getSimpleName()+":"+request;
    }

    public static void save(String request,DataModel requestmodel,Object result)
    {
        try
        {
            request=request.replaceAll(":","@=");
            String json=JSON.toJSONString(result);
            if(result instanceof DataModel)
            {
                cacheOne.put(cacheKey(requestmodel.getClass(),request), json);
            }
            else
            {
                cacheList.put(cacheKey(requestmodel.getClass(),request), json);
            }
            Field[] fields = requestmodel.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }
                properties.append(propertyKey(requestmodel),request);
            }
            String finalRequest = request;
            if(iHasForeignKeys.class.isAssignableFrom(requestmodel.getClass()))
            {
                ((iHasForeignKeys)requestmodel).foreignClassAndMyColumn().foreach((a, b)->{
                    b.forEach(c->{
                        try
                        {
                            Field field = requestmodel.getClass().getDeclaredField(c);
                            Object value = field.get(requestmodel);
                            if (value != null)
                            {
                                properties.append(propertyKey(requestmodel), finalRequest);
                            }
                        }
                        catch (Exception e)
                        {
                            iApp.error(e);
                        }
                    });
                });
            }

            if(result instanceof DataResult)
            {
                KeyArrayMap<Class<? extends DataModel>,Integer> arr= ((DataResult)result).idListData();
                arr.foreach((Class<? extends DataModel> cls,ArrayList<Integer> idd)->{
                    idd.forEach(id->{
                        if(id==null)
                        {
                            return;
                        }
                        String key=idKey(cls,id);
                        ids.append(key,finalRequest);
                    });
                });
            }
            else
            {
                String key=idKey((Class<? extends DataModel>) result.getClass(),((DataModel)result).id());
                ids.append(key,finalRequest);
            }
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    public static JSON get(String request,DataModel requestmodel)
    {
        request=request.replaceAll(":","@=");
        String key=cacheKey(requestmodel.getClass(),request);
        KeyRedis cache=cacheList;
        if(requestmodel.option().containsKey(DatabaseOptions.limit)
                && requestmodel.option().get(DatabaseOptions.limit).size()>0
        )
        {
            ArrayList<String> list=requestmodel.option().get(DatabaseOptions.limit);
            if(list.get(list.size()-1).trim().equals("1"))
            {
                cache=cacheOne;
            }
        }
        if(!cache.containsKey(key))
        {
            return null;
        }
        String val= cache.get(key);
        if(val==null)
        {
            return null;
        }
        val=val.trim();
        if(val.startsWith("["))
        {
            iApp.debug();
            return JSON.parseArray(val);
        }
        return JSON.parseObject(val);
    }
    @Override
    public void onDataDeleted(DataModel model)
    {
        try
        {
            iApp.debug(this.getClass().getSimpleName()+".onDataDeleted",model.toString());
            if(iHasOtherCacheTriggerModels.class.isAssignableFrom(model.getClass()))
            {
                ((iHasOtherCacheTriggerModels)model).triggerUpdateModelsOnDeleted().forEachForcely(a->{
                    new ExtendThread(()->{
                        onDataUpdated(a);
                    }).start();
                });
            }
            Object id=model.id();
            if(id==null)
            {
                iApp.error("onDataDeleted id为null:"+model.toString());
                return;
            }
            String key=idKey(model.getClass(),id);
            if(!ids.containsKey(key))
            {
                iApp.debug(this.getClass().getSimpleName()+".onDataDeleted","ids 不存在："+key);
                return;
            }
            DataResult<String> list=DataResult.castE(ids.get(key));

            for (int i = 0; i < list.size(); i++)
            {
                String request=list.get(i);
                iApp.debug(this.getClass().getSimpleName()+".onDataDeleted","request："+request);
                ArrayList<String> oneKeyList=cacheOne.keys("*:"+request);
                if(oneKeyList.size()>0)
                {
                    for (int i1 = 0; i1 < oneKeyList.size(); i1++)
                    {
                        cacheOne.remove(oneKeyList.get(i1));
                        iApp.debug(this.getClass().getSimpleName()+".onDataDeleted","oneKeyList handled："+oneKeyList.get(i1));
                    }
                    continue;
                }

                ArrayList<String> listKeyList=cacheList.keys("*:"+request);
                if(listKeyList.size()==0)
                {
                    continue;
                }
                for (int i1 = 0; i1 < listKeyList.size(); i1++)
                {
                    String totalKey=listKeyList.get(i1);
                    cacheList.remove(listKeyList.get(i1));
                    iApp.debug(this.getClass().getSimpleName()+".onDataDeleted","listKeyList handled："+listKeyList.get(i1));
                }
            }
            ids.remove(key);
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    @Override
    public void onDataInserted(DataModel model)
    {
        try
        {
            iApp.debug(this.getClass().getSimpleName()+".onDataInserted",model.toString());
            if(iHasOtherCacheTriggerModels.class.isAssignableFrom(model.getClass()))
            {
                ((iHasOtherCacheTriggerModels)model).triggerUpdateModelsOnInserted().forEachForcely(a->{
                    new ExtendThread(()->{
                        onDataUpdated(a);
                    }).start();
                });
            }
            ArrayList<String> keys=properties.keys(model.getClass().getSimpleName()+":*");
            JSONObject obj=JSON.parseObject(model.toStringWithOutNull());
            obj.remove("id");
            DataResult<String> checkList=new DataResult<>();
            ConcurrentHashMap<String,String> map=new ConcurrentHashMap<>();
            obj.forEach((key,val)->{
                String tag="\""+key+"\"@=";
                checkList.add(tag);
                if(val==null)
                {
                    map.put(tag,"\""+key+"\"@=null");
                }
                else if(val instanceof Integer||val instanceof Boolean||val instanceof Long)
                {
                    map.put(tag,"\""+key+"\"@="+val);

                }
                else {
                    map.put(tag,"\""+key+"\"@=\""+val+"\"");
                }
            });

            DataResult<String> updateKeys=new DataResult<>();
            for (int i = 0; i < keys.size(); i++)
            {
                boolean qualified=true;
                String key=keys.get(i);

                for (int i1 = 0; i1 < checkList.size(); i1++)
                {
                    String tag=checkList.get(i1);
                    if(key.contains(tag)
                    && !key.contains(map.get(tag))
                    )
                    {
                        qualified=false;
                    }
                }
                if(qualified)
                {
                    updateKeys.add(key);
                }
            }

            iApp.debug(this.getClass().getSimpleName()+":updateKeys",updateKeys.toString());
            for (int i = 0; i < updateKeys.size(); i++)
            {
                if(!properties.containsKey(updateKeys.get(i)))
                {
                    continue;
                }
                DataResult<String> list=DataResult.castE(properties.get(updateKeys.get(i)));
                for (int i1 = 0; i1 < list.size(); i1++)
                {
                    try
                    {
                        String request=list.get(i1);
                        request=request.replaceAll("@=",":");
                        iApp.debug("forceSelectApi.request",request);
                        String finalRequest = request;
                        new ExtendThread(()->{
                            try
                            {
                                SelectApi.forceSelectApi.getJSON(JSONObject.parseObject(finalRequest), 0);
                            }
                            catch (Exception e)
                            {
                                iApp.error(e);
                            }
                        }).start();
                    }
                    catch (Exception e)
                    {
                        iApp.error(e);
                    }
                }
            }
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }
    public void onMultiDataUpdated(DataResult<? extends DataModel> allModels)
    {
        try
        {
            iApp.debug(this.getClass().getSimpleName()+".onMultiDataUpdated",allModels.toString());
            ConcurrentHashMap<String,Boolean> map=new ConcurrentHashMap<>();
            allModels.forEachForcely(model->{
                if(iHasOtherCacheTriggerModels.class.isAssignableFrom(model.getClass()))
                {
                    onMultiDataUpdated(((iHasOtherCacheTriggerModels)model).triggerUpdateModelsOnUpdated());
                }
                Object id=model.id();
                if(id==null)
                {
                    iApp.error("onDataUpdated id为null:"+model.toString());
                    return;
                }
                String key=idKey(model.getClass(),id);
                if(!ids.containsKey(key))
                {
                    return;
                }
                DataResult<String> list=DataResult.castE(ids.get(key));

                list.forEach(request->{map.put(request,true);});
            });

            DataResult<String> list=DataResult.castE(map.keySet());

            for (int i = 0; i < list.size(); i++)
            {
                String request = list.get(i);
                request=request.replaceAll("@=",":");
                iApp.debug("forceSelectApi.request",request);
                try
                {
                    String finalRequest = request;
                    new ExtendThread(()->{
                        try
                        {
                            SelectApi.forceSelectApi.getJSON(JSONObject.parseObject(finalRequest), 0);
                        }
                        catch (Exception e)
                        {
                            iApp.error(e);
                        }
                    }).start();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    @Override
    public void onDataUpdated(DataModel model)
    {
        try
        {
            iApp.debug(this.getClass().getSimpleName()+".onDataUpdated",model.toString());
            Object id=model.id();
            if(id==null)
            {
                iApp.error("onDataUpdated id为null:"+model.toString());
                return;
            }
            String key=idKey(model.getClass(),id);
            if(!ids.containsKey(key))
            {
                return;
            }
            DataResult<String> list=DataResult.castE(ids.get(key));
            if(iHasOtherCacheTriggerModels.class.isAssignableFrom(model.getClass()))
            {
                ((iHasOtherCacheTriggerModels)model).triggerUpdateModelsOnUpdated().forEachForcely(a->{
                    new ExtendThread(()->{
                        onDataUpdated(a);
                    }).start();
                });
            }
            for (int i = 0; i < list.size(); i++)
            {
                String request = list.get(i);
                request=request.replaceAll("@=",":");
                iApp.debug("forceSelectApi.request",request);
                try
                {
                    String finalRequest = request;
                    new ExtendThread(()->{
                        try
                        {
                            SelectApi.forceSelectApi.getJSON(JSONObject.parseObject(finalRequest), 0);
                        }
                        catch (Exception e)
                        {
                            iApp.error(e);
                        }
                    }).start();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
            this.onDataInserted(model);
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }
}
