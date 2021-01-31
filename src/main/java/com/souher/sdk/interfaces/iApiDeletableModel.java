package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public interface iApiDeletableModel
{
    default String[] inNeedDeleteFields()
    {
        return new String[]{"id"};
    }

    default DataModel filterDelete()  throws Exception
    {
        return (DataModel) this;
    }

    static DataResult<? extends DataModel> parseJSON(JSONObject m) throws Exception
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
                if(!iApiDeletableModel.class.isAssignableFrom(cls))
                {
                    throw new Exception("未授权删除");
                }

                DataModel model=cls.newInstance();
                String[] hides=((iApiDeletableModel)model).inNeedDeleteFields();
                Field[] fields= cls.getDeclaredFields();
                JSONObject jsonObject=(JSONObject) b;

                ArrayList<String> all = new ArrayList<>(jsonObject.keySet());
                ArrayList<String> need = new ArrayList<String>();
                need.addAll(Arrays.asList(hides));
                for (int i = 0; i <fields.length; i++)
                {
                    Field field=fields[i];
                    if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                    {
                        continue;
                    }

                    if(jsonObject.containsKey(field.getName()))
                    {
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
                    throw new Exception("未授权:"+cls.getSimpleName().toLowerCase()+":"+String.join(",",all));
                }
                if(need.size()>0)
                {
                    throw new Exception("必须字段未包含："+cls.getSimpleName().toLowerCase()+":"+String.join(",",need));
                }
                dataModels.add(model);
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        });
        return dataModels;
    }
}
