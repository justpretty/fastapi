package com.souher.sdk.interfaces;

import com.alibaba.fastjson.JSON;
import com.souher.sdk.business.CacheModelWorker;
import com.souher.sdk.business.CacheRequestWorker;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.database.DatabaseOptions;
import com.souher.sdk.extend.KeyMapMap;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public interface iHasCountColumns extends iOnDataInserted, iOnDataDeleted, iMultiModelWatcher,iOnEveryMinute
{
    ConcurrentHashMap<String,KeyMapMap<Class<? extends DataModel>,String,String>> countColumnsData=new ConcurrentHashMap<>();

    String[] countCoumns();

    default int countColumnTriggerCachedIntervalMinutes()
    {
        return 0;
    }


    default void onEveryMinute(Long tick) throws Exception
    {
        if(countColumnTriggerCachedIntervalMinutes()<=0|| countColumnTriggerCachedIntervalMinutes()>59)
        {
            return;
        }
        Date date=new Date(tick);
        String minuteString=iApp.m.format(date);
        int minute=Integer.parseInt(minuteString);
        if(minute% countColumnTriggerCachedIntervalMinutes()!=0)
        {
            return;
        }
        DataModel model=((Class<? extends DataModel> )this.getClass()).newInstance();
        model.option().append(DatabaseOptions.raw,"(#TABLE#.`count_change_time` >= '"+iApp.yyyyMMddHHmmss.format(new Date(tick- (long) countColumnTriggerCachedIntervalMinutes() *60*1000))+"' and #TABLE#.`count_change_time`=#TABLE#.`update_datetime`)");
        DataResult<? extends DataModel> all=model.selectSimilarModels((Class<? extends DataModel> )this.getClass());
        CacheModelWorker.Current.clearAllCache((Class<? extends DataModel>) this.getClass());
        CacheRequestWorker.Current.onMultiDataUpdated(all);
    }


    default void onDataInserted(DataModel model)
    {
//        iApp.debug(model.getClass().getSimpleName(),"iHasCountColumns.onDataInserted");
        AtomicBoolean isLoaded= new AtomicBoolean(false);
        countColumnsData.forEach((a,b)->
        {
            if (!b.containsKey(model.getClass()))
            {
                return;
            }
            isLoaded.set(true);
        });
        if(!isLoaded.get())
        {
            for (int i = 0; i < countCoumns().length; i++)
            {
                String item= countCoumns()[i];
                String[] ii=item.split(":");
                if(ii.length!=4)// 我的coount字段，类名，我的连接字段，他的连接字段
                {
                    iApp.error("count 字段 配置错误："+item);
                    continue;
                }
                Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(ii[1]);
                if(cls==null)
                {
                    iApp.error("count 字段 配置错误：未知表："+ii[1]);
                    continue;
                }
                KeyMapMap keyMapMap=new KeyMapMap();
                keyMapMap.put(cls,ii[2],ii[3]);
                countColumnsData.put(ii[0],keyMapMap);
            }
        }
        countColumnsData.forEach((a,b)->{
            if(!b.containsKey(model.getClass()))
            {
                return;
            }
            b.get(model.getClass()).forEach((c,d)->{
            try
            {
                Class<? extends DataModel> cls= (Class<? extends DataModel>) this.getClass();
                DataModel dataModel =cls.newInstance();
                Field my=cls.getDeclaredField(c);
                Field his=model.getClass().getDeclaredField(d);
                Object e=his.get(model);
                if(e==null)
                {
                    return;
                }
                my.set(dataModel,e);
                dataModel.increase(a);
                dataModel.setUpdateWithoutId();
                if(countColumnTriggerCachedIntervalMinutes()>0)
                {
                    Field count_change_time=cls.getDeclaredField("count_change_time");
                    count_change_time.set(dataModel,iApp.yyyyMMddHHmmss.format(new Date()));
                    dataModel.setUpdateWithoutTrigger();
                }
                iApp.debug(this.getClass().getSimpleName()+"."+iHasCountColumns.class.getSimpleName()+".onDataInserted", JSON.toJSONString(dataModel));
                dataModel.save();
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
            });
        });
    }

    default void onDataDeleted(DataModel model)
    {
//        iApp.debug(model.getClass().getSimpleName(),"iHasCountColumns.onDataDeleted");
        AtomicBoolean isLoaded= new AtomicBoolean(false);
        countColumnsData.forEach((a,b)->
        {
            if (!b.containsKey(model.getClass()))
            {
                return;
            }
            isLoaded.set(true);
        });
        if(!isLoaded.get())
        {
            for (int i = 0; i < countCoumns().length; i++)
            {
                String item= countCoumns()[i];
                String[] ii=item.split(":");
                if(ii.length!=4)// 我的coount字段，类名，我的连接字段，他的连接字段
                {
                    iApp.error("count 字段 配置错误："+item);
                    continue;
                }
                Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(ii[1]);
                if(cls==null)
                {
                    iApp.error("count 字段 配置错误：未知表："+ii[1]);
                    continue;
                }
                KeyMapMap keyMapMap=new KeyMapMap();
                keyMapMap.put(cls,ii[2],ii[3]);
                countColumnsData.put(ii[0],keyMapMap);
            }
        }

        countColumnsData.forEach((a,b)->{
            if(!b.containsKey(model.getClass()))
            {
                return;
            }

            b.get(model.getClass()).forEach((c,d)->{
                try
                {
                    Class<? extends DataModel> cls= (Class<? extends DataModel>) this.getClass();
                    DataModel dataModel =cls.newInstance();
                    Field my=cls.getDeclaredField(c);
                    Field his=model.getClass().getDeclaredField(d);
                    Object e=his.get(model);
//                    iApp.debug(3+c+":"+d,"deletecount");
//                    iApp.debug(4+model.toString(),"deletecount");
                    if(e==null)
                    {
                        return;
                    }

                    my.set(dataModel,e);
                    dataModel.decrease(a);
                    dataModel.setUpdateWithoutId();
                    dataModel.save();
//                    iApp.debug(4+dataModel.toString(),"deletecount");
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        });
    }
}
