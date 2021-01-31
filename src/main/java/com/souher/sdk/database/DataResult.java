package com.souher.sdk.database;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.souher.sdk.extend.KeyArrayMap;
import com.souher.sdk.extend.SystemConsumer;
import com.souher.sdk.extend.SystemFunction;
import com.souher.sdk.iApp;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class  DataResult<E> extends ArrayList<E>
{

    Field[] fields=null;
    KeyArrayMap<Class<? extends DataModel>,Integer> idListData=new KeyArrayMap<>();

    public void addId(Class<? extends DataModel> cls,Integer id)
    {
        idListData.append(cls,id);
    }
    public void clearAllIds()
    {
        idListData=new KeyArrayMap<>();
    }

    public KeyArrayMap<Class<? extends DataModel>,Integer> idListData()
    {
        return idListData;
    }

    AtomicReference<String> errorMessage=new AtomicReference<>();
    public AtomicReference<String> errorMessage()
    {
        return errorMessage;
    }

    public static <T> DataResult<T> castList(List<T> list)
    {
        DataResult<T> dataResult=new DataResult<>();
        dataResult.addAll(list);
        return dataResult;
    }

    public static <T> DataResult<T> of(T... t)
    {
        return castArray(t);
    }

    public static <T> DataResult<T> castEnumeration(Enumeration<T> e)
    {
        DataResult<T> dataResult=new DataResult<>();
        while(e.hasMoreElements()){
            dataResult.add(e.nextElement());
        }
        return dataResult;
    }

    public static <T> DataResult<T> castE(Collection<T> list)
    {
        DataResult<T> dataResult=new DataResult<>();
        dataResult.addAll(list);
        return dataResult;
    }

    public static <T,V> DataResult<T> getKeys(ConcurrentHashMap<T,V> b)
    {
        DataResult<T> dataResult=new DataResult<>();
        for(T key : b.keySet()) {
            dataResult.add(key);
        }
        return dataResult;
    }

    public static <T> DataResult<T> castArray(T[] array)
    {
        DataResult<T> dataResult=new DataResult<>();
        for(int i=0;i<array.length;i++)
        {
            dataResult.add(array[i]);
        }
        return dataResult;
    }

    public boolean checkContainsPart(String wholeline)
    {
        AtomicBoolean result= new AtomicBoolean(false);
        for(int i=0;i<this.size();i++)
        {
            String a=this.get(i).toString();
            if(wholeline.contains(a))
            {
                result.set(true);
                break;
            }
        }
        return result.get();
    }

    public void forEachForcely(SystemConsumer<? super E> action) throws Exception
    {
        for (int i = 0,lengh=this.size(); i < lengh; i++) {
            try
            {
                action.accept(DataResult.this.get(i));
            }
            catch (Exception e)
            {
                throw e;
            }
        }
    }

    public boolean forEachForcely(SystemFunction<? super E,Boolean> action)
    {
        for (int i = 0,lengh=this.size(); i < lengh; i++) {
            try
            {
                boolean continuing=action.accept(DataResult.this.get(i));
                if(!continuing)
                {
                    return false;
                }
            }
            catch (Exception e)
            {
                errorMessage.set(e.getMessage());
                iApp.error(e);
                return false;
            }
        }
        return true;
    }

    public void forEachStopOnError(SystemConsumer<? super E> action)
    {
        for (int i = 0,lengh=this.size(); i < lengh; i++) {
            try
            {
                action.accept(DataResult.this.get(i));
            }
            catch (Exception e)
            {
                iApp.error(e);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String toFormatString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        try
        {
            for (E a : this) {
                if (fields == null) {
                    fields = a.getClass().getDeclaredFields();
                }
                for (Field field : fields) {
                    if (field.get(a) == null) {
                        sb.append(field.getName()).append(":null|");
                    }
                    else
                    {
                        sb.append(field.getName()).append(':').append(field.get(a).toString()).append('|');
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
        return null;
    }
}
