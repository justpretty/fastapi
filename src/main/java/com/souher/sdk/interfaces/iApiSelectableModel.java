package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.database.DatabaseOptions;
import com.souher.sdk.extend.FourObject;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.iString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface iApiSelectableModel
{
    long maxPageSize=30;

    default String[] nonSearchableFields()
    {
        return new String[]{"create_datetime","update_datetime"};
    }

    default String deleteField(){
        return "";
    }

    default long pageSize()
    {
        return 25;
    }
    default DataModel filterSelect(int userid)  throws Exception
    {
        return (DataModel) this;
    }

    default String[] orderFields()
    {
        return new String[]{"update_datetime","desc"};
    }

    static  <T extends DataModel> DataResult<DataModel> parseJSON(JSONObject m,int userid) throws Exception
    {
        DataResult<DataModel> dataModels=new DataResult<>();
        DataResult.castE(m.keySet()).forEachForcely(a->{
            Object b=m.get(a);
            if(b==null)
            {
                return ;
            }
            a=a.toLowerCase();

            Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(a);
            if(cls==null||!iApiSelectableModel.class.isAssignableFrom(cls))
            {
                throw new Exception(cls==null?"未授权"+"":"未授权:"+cls.getSimpleName().toLowerCase());
            }
            DataModel model=cls.newInstance();
            String[] hides=((iApiSelectableModel)model).nonSearchableFields();
            Field[] fields= cls.getDeclaredFields();
            boolean isList=false;
            JSONObject jsonObject=(JSONObject) b;
            JSONObject option=new JSONObject();
            long offset=0;
            long limit =0;
            boolean count=false;
            String order="";
            String raw="";
            JSONObject sub=new JSONObject();
            DataResult<DataModel> join=new DataResult<>();
            JSONObject on=new JSONObject();
            JSONArray selectFields=new JSONArray();
            String tableAlias=null;
            if(((JSONObject) b).containsKey("_"))
            {
                option=((JSONObject) b).getJSONObject("_");
                ((JSONObject) b).remove("_");
                if(option.containsKey("noncache"))
                {
                    model.noncache=true;
                }
                if(option.containsKey("alias"))
                {
                    tableAlias=option.getString("alias");
                }
                if(option.containsKey("offset"))
                {
                    offset=option.getLong("offset");
                }
                if(option.containsKey("limit"))
                {
                    limit=option.getLong("limit");
                    if(limit>maxPageSize&&limit>((iApiSelectableModel)model).pageSize())
                    {
                        limit=Math.max(maxPageSize,((iApiSelectableModel)model).pageSize());
                    }
                }
                if(option.containsKey("count"))
                {
                    count=true;
                }
                if(option.containsKey("order"))
                {
                    order=option.getString("order");
                }
                if(option.containsKey("raw"))
                {
                    raw=option.getString("raw");
                }
                if(option.containsKey("join"))
                {
                    join=parseJSON(option.getJSONObject("join"),userid);
                }
                if(option.containsKey("on"))
                {
                    on=option.getJSONObject("on");
                }
                if(option.containsKey("sub"))
                {
                    sub=option.getJSONObject("sub");
                }
                if(option.containsKey("fields"))
                {
                    selectFields=option.getJSONArray("fields");
                }
            }
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                if(entry.getKey().replaceAll("[0-9]*","").isEmpty())
                {
                    isList=true;
                    offset=Long.parseLong(entry.getKey());
                    jsonObject= (JSONObject) entry.getValue();
                    break;
                }
            }
            ArrayList<String> all = new ArrayList<>(jsonObject.keySet());
            for (int i = 0; i <fields.length; i++)
            {
                Field field=fields[i];
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }

                if(jsonObject.containsKey(field.getName()))
                {
                    for (int i1 = 0; i1 < hides.length; i1++)
                    {
                        if(field.getName().equals(hides[i1]))
                        {
                            throw new Exception("未授权:"+cls.getSimpleName().toLowerCase()+":"+field.getName());
                        }
                    }
                    Object object=jsonObject.get(field.getName());
                    if(field.getType().equals(Long.class)&& object!=null)
                    {
                        object=((Number)object).longValue();
                    }

                    field.set(model,object);
                    all.remove(field.getName());
                }
            }
            if(all.size()>0)
            {
                throw new Exception("未授权:"+cls.getSimpleName().toLowerCase()+":"+String.join(",",all));
            }

            model.limit(offset,limit);
            if(count)
            {
                model.selectCount();
            }
            if(!order.isEmpty())
            {
                String[] orders=order.split("\\s+|,");
//                iApp.debug(Arrays.toString(orders),"orders");
                String lastColumn="";
                for (int i = 0; i < orders.length; i++)
                {
                    if(lastColumn.isEmpty())
                    {
                        lastColumn=orders[i];
                        continue;
                    }
                    else if(orders[i].equals("desc"))
                    {
                        model.orderDescBy(lastColumn);
                        lastColumn="";
                        continue;
                    }
                    else if(orders[i].equals("asc"))
                    {
                        model.orderAscBy(lastColumn);
                        lastColumn="";
                        continue;
                    }
                    model.orderAscBy(lastColumn);
                    lastColumn=orders[i];
                }
                if(!lastColumn.isEmpty())
                {
                    model.orderAscBy(lastColumn);
                }
//                iApp.debug(String.join(",",model.option().get(DatabaseOptions.orders)),"orders seted");
            }
            if(tableAlias!=null)
            {
                model.option().append(DatabaseOptions.table_alias,tableAlias);
            }
            if(on.keySet().size()>0)
            {
                on.forEach((c,d)->{
                    model.option().append(DatabaseOptions.onmy,c);
                    model.option().append(DatabaseOptions.onhis,d.toString());
                });
            }
            if(join.size()>0)
            {
                join.forEachForcely(c->{
                    c.option().remove(DatabaseOptions.limit);
                    c.option().remove(DatabaseOptions.orders);
                    model.join(c,c.option().get(DatabaseOptions.onmy).get(0),c.option().get(DatabaseOptions.onhis).get(0));
                });
            }
            if(selectFields.size()>0)
            {
                String[] d=new String[]{};
                model.select(selectFields.toArray(d));
            }
            if(!raw.isEmpty())
            {
                if(raw.contains("#SUB#"))
                {
                    DataModel submodel=parseJSON(sub,userid).get(0);
                    String subSql=DataModel.getDatabase().getSqlTemplate(submodel,submodel.databaseOptions(),new AtomicInteger(),new ArrayList());
                    subSql=subSql.replaceAll("limit 0,0","");
                    iApp.debug(subSql,"subsql");
                    raw=raw.replaceAll("#SUB#","("+subSql+")");
                }
                model.option().append(DatabaseOptions.raw,raw);
            }
            if(!((iApiSelectableModel)model).deleteField().isEmpty())
            {
                model.option().append(DatabaseOptions.isnull,((iApiSelectableModel)model).deleteField());
            }
//            ((iApiSelectableModel) model).filterSelect(userid);
            dataModels.add(model);

        });

        return dataModels;
    }
}
