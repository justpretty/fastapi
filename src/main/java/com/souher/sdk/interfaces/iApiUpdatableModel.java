package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public interface iApiUpdatableModel
{
    default String[] nonUpdatableFields()
    {
        return new String[]{"create_datetime","update_datetime"};
    }

    default String[] inNeedUpdatableFields()
    {
        return new String[]{};
    }

    default DataModel filterUpdate() throws Exception
    {
        return (DataModel) this;
    }

    static  <T extends DataModel> DataResult<DataModel> parseJSON(JSONObject m) throws Exception
    {
        DataResult<DataModel> dataModels=new DataResult<>();
        m.keySet().forEach(a->{
            try
            {
                Object b=m.get(a);
                if(b==null)
                {
                    throw new Exception("不得为空");
                }
                a=a.toLowerCase();
                Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(a);
                if(!iApiUpdatableModel.class.isAssignableFrom(cls))
                {
                    throw new Exception("未授权修改");
                }

                DataModel model=cls.newInstance();
                String[] inneeds=((iApiUpdatableModel)model).inNeedUpdatableFields();
                String[] hides=((iApiUpdatableModel)model).nonUpdatableFields();
                Field[] fields= cls.getDeclaredFields();
                if(b instanceof JSONArray)
                {
                    for(Object object : (JSONArray)b)
                    {
                        model=cls.newInstance();
                        iApp.debug("is_array:true");
                        JSONObject jsonObject=JSONObject.parseObject(object.toString());
                        handleJsonObject(dataModels, cls, model, inneeds, hides, fields, jsonObject);
                    }
                }
                else
                {
                    JSONObject jsonObject = (JSONObject) b;
                    handleJsonObject(dataModels, cls, model, inneeds, hides, fields, jsonObject);
                }
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        });
//        iApp.debug(dataModels.size()+"","dataModels.length");
        return dataModels;
    }

    static <T extends DataModel> void handleJsonObject(DataResult<DataModel> dataModels, Class<T> cls, DataModel model, String[] inneeds, String[] hides, Field[] fields, JSONObject jsonObject) throws Exception
    {
        ArrayList<String> all = new ArrayList<>(jsonObject.keySet());
        ArrayList<String> need = new ArrayList<String>();
        need.addAll(Arrays.asList(inneeds));
        ArrayList<String> hide = new ArrayList<String>(Arrays.asList(hides));
        for (int i = 0; i < fields.length; i++)
        {
            Field field= fields[i];
            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
            {
                continue;
            }

            if(jsonObject.containsKey(field.getName()))
            {
                if(hide.contains(field.getName()))
                {
                    throw new Exception("未授权:"+ cls.getSimpleName().toLowerCase()+":"+field.getName());
                }
                Object object=jsonObject.get(field.getName());
                if(field.getType().equals(Long.class)&& object!=null)
                {
                    object=((Number)object).longValue();
                }


                field.set(model,object);
                all.remove(field.getName());
                need.remove(field.getName());
            }
        }
        if(all.size()>0)
        {
            throw new Exception("未授权:"+ cls.getSimpleName().toLowerCase()+":"+String.join(",",all));
        }
        if(need.size()>0)
        {
            throw new Exception("必须字段未包含："+ cls.getSimpleName().toLowerCase()+":"+String.join(",",need));
        }
        dataModels.add(model);
    }
}
