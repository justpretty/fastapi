package com.souher.sdk.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.api.SelectApi;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.ExtendThread;
import com.souher.sdk.extend.KeyMapMapRedis;
import com.souher.sdk.extend.KeySetMapRedis;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import com.souher.sdk.interfaces.iAllModelWatcher;
import com.souher.sdk.interfaces.iOnDataDeleted;
import com.souher.sdk.interfaces.iOnDataSaved;
import com.souher.sdk.interfaces.iRedisModel;
import com.souher.sdk.model.CacheTask;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CacheRequestWorker2
//        implements iOnDataSaved, iOnDataDeleted, iAllModelWatcher
{
//    public static CacheRequestWorker2 Current=new CacheRequestWorker2();
//    public static KeyMapMapRedis cache=new KeyMapMapRedis(CacheRequestWorker2.class.getSimpleName());
//    public static KeySetMapRedis modelReqeustMap=new KeySetMapRedis(CacheRequestWorker2.class.getSimpleName());
//
//    public void init()
//    {
//        new ExtendThread(()->{
//            try
//            {
//                DataResult<CacheTask> result= DataModel.all(CacheTask.class);
//                result.forEachForcely(a->{
//                    modelReqeustMap.append(a.api_tag,a.request);
//                    Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(a.api_tag);
//                    createNewCache(a.request,a.user_id,cls);
//                });
//            }
//            catch (Exception e)
//            {
//                iApp.error(e);
//            }
//        }).start();
//    }
//
//    public void clear()
//    {
//        new ExtendThread(()->{
//            try
//            {
//                DataResult<CacheTask> result= DataModel.all(CacheTask.class);
//                result.forEachForcely(a->{
//                    Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(a.api_tag);
//                    clearAllCache(cls);
//                });
//            }
//            catch (Exception e)
//            {
//                iApp.error(e);
//            }
//        }).start();
//    }
//    public static void save(String request, Object a, int userid)
//    {
//        save( request,  a,  userid,null);
//    }
//    public static void save(String request, Object result, int userid,Class<? extends DataModel> inputcls)
//    {
//        new ExtendThread(()->{
//            if(userid<0)
//            {
//                return;
//            }
//            Class<? extends DataModel> cls;
//            DataModel c;
//            boolean allowNoId=false;
//            if(result instanceof DataModel)
//            {
//                cls= (Class<? extends DataModel>) result.getClass();
//                c= (DataModel) result;
//            }
//            else if(result instanceof DataResult)
//            {
//                DataResult b=(DataResult)result;
//                if(inputcls!=null)
//                {
//                    cls=inputcls;
//                    try
//                    {
//                        if(b.size()==0)
//                        {
//                            c=cls.newInstance();
//                            allowNoId=true;
//                        }
//                        else
//                        {
//                            c= (DataModel) b.get(0);
//                        }
//                    }
//                    catch (Exception e)
//                    {
//                        iApp.error(e);
//                        return;
//                    }
//                }
//                else if(b.size()==0)
//                {
//                    return;
//                }
//                else
//                {
//                    c= (DataModel) b.get(0);
//                    cls=c.getClass();
//                }
//            }
//            else
//            {
//                return;
//            }
//            try
//            {
//                if(!allowNoId&&!c.hasId())
//                {
//                    return;
//                }
//
//                String apiname=cls.getSimpleName().toLowerCase();
//                String cacheKey=cacheKey(cls,request);
//                cache.put(cacheKey, String.valueOf(userid),JSONObject.toJSONString(result));
//                iApp.debug(CacheRequestWorker2.class.getSimpleName()+".save",apiname+":"+userid+":"+JSONObject.toJSONString(result));
//                if(!modelReqeustMap.containsObject(apiname,request))
//                {
//                    modelReqeustMap.append(apiname,request);
//                    CacheTask.save(apiname, request, userid);
//                }
//            }
//            catch (Exception e)
//            {
//                iApp.error(e);
//                return;
//            }
//        }).start();
//    }
//    private static String cacheKey(Class<? extends DataModel> cls,String request)
//    {
//        String apiname=cls.getSimpleName().toLowerCase();
//
//        return apiname+":"+request;
//    }
//    public static JSON get(String request,DataModel a,int userid)
//    {
//        if(userid<0)
//        {
//            return null;
//        }
//        String cacheKey=cacheKey(a.getClass(),request);
//        if(CacheRequestWorker2.cache.containsKey(cacheKey)
//                && CacheRequestWorker2.cache.get(cacheKey, String.valueOf(userid))!=null
//        )
//        {
//            String val= CacheRequestWorker2.cache.get(cacheKey, String.valueOf(userid));
//            iApp.debug(CacheRequestWorker2.class.getSimpleName()+".get",a.getClass().getSimpleName()+":"+userid+":"+JSONObject.toJSONString(val));
//            if(val.trim().startsWith("["))
//            {
//                return JSON.parseArray(val);
//            }
//            else
//            {
//                return JSON.parseObject(val);
//            }
//        }
//        return null;
//    }
//
//    public void createNewCache(String request,Integer userid,Class<? extends DataModel> cls) throws Exception
//    {
//        JSONObject obj=new JSONObject();
//        obj.put(cls.getSimpleName().toLowerCase(),JSONObject.parseObject(request));
//        SelectApi.forceSelectApi.getJSON(obj,userid);
//        if(userid>0)
//        {
//            SelectApi.forceSelectApi.getJSON(obj,0);
//        }
//    }
//
//    public void clearAllCache(Class<? extends DataModel> cls)
//    {
//        String jedisKey = this.getClass().getSimpleName()+":"+cls.getSimpleName().toLowerCase() ;
//        iApp.debug(this.getClass().getSimpleName()+".clearAllCache.jedisKey",jedisKey);
//        Jedis jedis = iRedis.getJedis();
//        jedis.del(jedisKey);
//        jedisKey+=":*";
//        ArrayList<String> keys = new ArrayList<>(DataResult.castE(jedis.keys(jedisKey)));
//        if(keys.size()==0)
//        {
//            jedis.close();
//            return;
//        }
//        iApp.debug(this.getClass().getSimpleName()+".clearAllCache.keys",String.join(",",keys));
//        jedis.del(keys.toArray(new String[0]));
//        jedis.close();
//    }
//    public synchronized void refreshCache(Class<? extends DataModel> cls) throws Exception
//    {
//        String api_tag=cls.getSimpleName().toLowerCase();
//        if(iRedisModel.class.isAssignableFrom(cls))
//        {
//            CacheModelWorker.Current.clearAllCache(cls);
//        }
//        if(modelReqeustMap.containsKey(api_tag))
//        {
//            //iApp.debug(this.getClass().getSimpleName()+".refreshCache.containsKey",api_tag+":"+modelReqeustMap.get(api_tag));
//            DataResult.castE(modelReqeustMap.get(api_tag)).forEachForcely(a->{
//                String cacheKey=api_tag+":"+a;
//                if(cache.containsKey(cacheKey))
//                {
//                   // iApp.debug(this.getClass().getSimpleName()+".refreshCache."+"recreated："+cacheKey+":"+cache.get(cacheKey).size());
//                    DataResult.castEnumeration(cache.get(cacheKey).keys()).forEachForcely(b->{
//                        createNewCache(a, Integer.valueOf(b),cls);
//                    });
//                }
//            });
//        }
//        Reflector.Default.getClassesByForeignClass(cls).forEachForcely(cls1->{
//            if(iRedisModel.class.isAssignableFrom(cls1))
//            {
//                CacheModelWorker.Current.clearAllCache(cls1);
//            }
//            String api_tag1=cls1.getSimpleName().toLowerCase();
//            iApp.debug(this.getClass().getSimpleName()+".refreshCache."+"iHasForeignKeys："+api_tag1);
//
//            DataResult.castE(modelReqeustMap.get(api_tag1)).forEachForcely(a->{
//                String cacheKey=api_tag1+":"+a;
//                if(cache.containsKey(cacheKey))
//                {
//                    iApp.debug(this.getClass().getSimpleName()+".refreshCache."+"recreated："+cacheKey+":"+cache.get(cacheKey).size());
//                    DataResult.castEnumeration(cache.get(cacheKey).keys()).forEachForcely(b->{
//                        createNewCache(a, Integer.valueOf(b),cls1);
//                    });
//                }
//            });
//        });
//    }
//
//    public static ConcurrentHashMap<String,Integer> fansIdFields(DataModel model) throws Exception
//    {
//        Class<? extends DataModel> cls=model.getClass();
//        Field[] fields=cls.getDeclaredFields();
//        ConcurrentHashMap<String,Integer> list=new ConcurrentHashMap<>();
//        for (int i = 0; i < fields.length; i++)
//        {
//            Field field=fields[i];
//            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
//            {
//                continue;
//            }
//            if(field.getName().endsWith("fans_id")
//            &&field.get(model)!=null
//            )
//            {
//                list.put(field.getName(), (int) field.get(model));
//            }
//        }
//        return list;
//    }
//
//    @Override
//    public void onDataSaved(DataModel model)
//    {
//        try
//        {
//            iApp.debug(this.getClass().getSimpleName()+".onDataSaved",model.toString());
//            refreshCache( model.getClass());
//        }
//        catch (Exception e)
//        {
//            iApp.error(e);
//        }
//    }
//
//    @Override
//    public void onDataDeleted(DataModel model)
//    {
//        try
//        {
//            iApp.debug(this.getClass().getSimpleName()+".onDataDeleted",model.toString());
//            refreshCache( model.getClass());
//        }
//        catch (Exception e)
//        {
//            iApp.error(e);
//        }
//    }
}
