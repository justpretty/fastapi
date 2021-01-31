package com.souher.sdk.api;

import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.InterfaceExecutor;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.interfaces.*;
import spark.Request;
import spark.Response;

import java.util.concurrent.Executor;

public class TriggerApi implements iApi
{
    @Override
    public boolean hasPost()
    {
        return true;
    }

    @Override
    public boolean hasGet()
    {
        return false;
    }

    @Override
    public String urlPath()
    {
        return "/trigger/*/*";
    }

    @Override
    public String handle(Request request, Response response)
    {

        JSONObject result=new JSONObject();
        try
        {
            String classname=request.splat()[0];
            String method=request.splat()[1];
            String  body=request.body();
            Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(classname);
            if(cls ==null)
            {
                result.put("error","non class:"+request.uri()+":"+body);
                return result.toString();
            }
            DataModel dataModel=JSONObject.parseObject(body,cls);

            iApp.debug("TriggerApi.handling",request.uri()+":"+body+"==>"+(dataModel!=null?dataModel.toStringWithOutNull():""));

            if(dataModel==null)
            {
                result.put("error","non datamodel:"+request.uri()+":"+body);
            }
            else if(DataResult.castArray(iAppConfig.webConfig().whiteiplist()).contains(request.ip()))
            {
                if(!method.equals("delete") &&JSONObject.parseObject(body).keySet().size()<cls.getDeclaredFields().length)
                {
                    DataModel finalDataModel = dataModel;

                    Integer id = (Integer) finalDataModel.getClass().getDeclaredField("id").get(finalDataModel);
                    if(id!=null)
                    {

                        dataModel = DataModel.first(dataModel.getClass(), a ->
                        {
                            try
                            {
                                a.noncache=true;
                                a.getClass().getDeclaredField("id").set(a, id);
                            }
                            catch (Exception e)
                            {
                                iApp.error(e);
                            }
                        });
                    }
                    iApp.debug("TriggerApi.first",JSONObject.toJSONString(dataModel));
                }
                switch (method)
                {
                    case "insert":
                        InterfaceExecutor.Current.executorOrderly(iOnDataInserted.class,dataModel);
                        InterfaceExecutor.Current.executorOrderly(iOnDataSaved.class,dataModel);
                        result.put("success","ok");
                        break;
                    case "update":
                        InterfaceExecutor.Current.executorOrderly(iOnDataUpdated.class,dataModel);
                        InterfaceExecutor.Current.executorOrderly(iOnDataSaved.class,dataModel);
                        result.put("success","ok");
                        break;
                    case "delete":
                        dataModel.delete();
                        result.put("success","ok");
                        break;
                    default:
                        result.put("error","method deny:"+method);
                        break;
                }
            }
            else
            {
                result.put("error","ip deny:"+request.ip());
            }
        }
        catch (Exception e)
        {
            iApp.error(e);
            result.put("error",e.getMessage()+e.getCause().getMessage());
        }
        iApp.debug("TriggerApi.handled",request.uri()+":"+request.body()+"==>"+result.toString());
        return result.toString();
    }
}
