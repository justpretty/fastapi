package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.KeyMapMap;
import com.souher.sdk.extend.SystemFunction;
import com.souher.sdk.extend.SystemHashMap;
import com.souher.sdk.iApp;
import com.souher.sdk.iString;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public interface iStatisticsDataCreatorForTwoDimensional
{
    default KeyMapMap<Class<? extends DataModel>,String,SystemFunction<DataModel,Integer>> statisticsCountFields()
    {
        return new KeyMapMap<>();
    }

    default KeyMapMap<Class<? extends DataModel>,String,SystemFunction<DataModel,Long>> statisticsSumFields()
    {
        return new KeyMapMap<>();
    }

    default void clearCount() throws Exception
    {
        statisticsCountFields().foreach((k, v)->{
            v.forEach((column,function)->{
                try
                {
                    Field field1=this.getClass().getDeclaredField(column);
                    field1.set(this,0L);
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        });
        statisticsSumFields().foreach((k, v)->
        {
            v.forEach((column, function) ->
            {
                try
                {
                    Field field1 = this.getClass().getDeclaredField(column);
                    field1.set(this, 0L);
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        });
    }

    default void handleItem(Class<? extends DataModel> itemClass,DataModel item, KeyMapMap<String,Integer,Long> countMap) throws Exception
    {
        statisticsCountFields().foreach((k, v)->
        {
            v.forEach((column, function) ->
            {
                try
                {
                    if(!k.equals(itemClass))
                    {
                        return;
                    }
                    Integer integer = function.accept(item);
                    if (integer == null)
                    {
                        return;
                    }
                    if (countMap.containsKey2(column, integer))
                    {
                        return;
                    }
                    countMap.put(column, integer, 9999L);
                    Field field1 = this.getClass().getDeclaredField(column);
                    field1.set(this, (long) countMap.get(column).size());
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        });
        statisticsSumFields().foreach((k, v)->
        {
            v.forEach((column, function) ->
            {
                try
                {
                    Field field1 = this.getClass().getDeclaredField(column);
                    long val = 0;
                    if (field1.get(this) != null)
                    {
                        val = (Long)field1.get(this);
                    }
                    field1.set(this, val + function.accept(item));
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            });
        });
    }

    default void setDate(String dateTime) throws Exception
    {
        Field datefield=this.getClass().getDeclaredField("date");
        Field year_field=this.getClass().getDeclaredField("year");
        Field month_field=this.getClass().getDeclaredField("month");
        Field day_field=this.getClass().getDeclaredField("day");
        Field half_year_number_field=this.getClass().getDeclaredField("half_year_number");
        Field season_number_field=this.getClass().getDeclaredField("season_number");
        Field week_number_field=this.getClass().getDeclaredField("week_number");
        datefield.set(this,dateTime.substring(0,10));
        Date date=iApp.yyyyMMddHHmmss.parse(dateTime);
        year_field.set(this,Integer.parseInt(iApp.format("yyyy").format(date)));
        month_field.set(this,Integer.parseInt(iApp.format("M").format(date)));
        day_field.set(this,Integer.parseInt(iApp.format("d").format(date)));
        half_year_number_field.set(this,((Integer)month_field.get(this) - 1) / 6 +1);
        season_number_field.set(this,((Integer)month_field.get(this) - 1) / 3 +1);
        week_number_field.set(this,iApp.weekNumber(date) +1);

    }
    default void handleDimensionals(Long tick, DataResult<Class<? extends DataModel>> itemClassList) throws Exception
    {
        Date date=new Date(tick);
        ((DataModel)this).orderDescBy("id");
        ((DataModel)this).first();
        Field datefield=this.getClass().getDeclaredField("date");
        if(datefield.get(this)!=null&&datefield.get(this).equals(iApp.yyyyMMdd.format(date)))
        {
            return;
        }
        AtomicReference<String> start= new AtomicReference<>("0000-00-00 00:00:00");
        AtomicReference<Date> startDate=new AtomicReference<>(null);
        boolean hasId=((DataModel)this).hasId();
        if(hasId)
        {
            startDate.set(iApp.yyyyMMdd.parse(datefield.get(this).toString()));
            startDate.get().setTime(startDate.get().getTime()+24*3600000);
            start.set(iApp.yyyyMMdd.format(startDate.get()) + " 00:00:00");
        }
        else
        {
            itemClassList.forEachForcely(itemClass->{
                DataModel model=DataModel.first(itemClass,a->{
                    a.orderAscBy("create_datetime");
                });
                if(!model.hasId())
                {
                    return;
                }
                Date createDate=iApp.yyyyMMddHHmmss.parse(itemClass.getDeclaredField("create_datetime").get(model).toString());
                if(startDate.get() ==null)
                {
                    startDate.set(new Date(createDate.getTime()));
                    return;
                }
                if(startDate.get().getTime()>createDate.getTime())
                {
                    startDate.get().setTime(createDate.getTime());
                }
            });
            String text=iApp.yyyyMMdd.format(startDate.get());
            startDate.set(iApp.yyyyMMdd.parse(text));
            start.set(text + " 00:00:00");
        }
        if(iApp.yyyyMMdd.format(date).startsWith(start.get()))
        {
            return;
        }
        long tickYesterdayNow=date.getTime()-3600000*24;
        while (startDate.get().getTime()<=tickYesterdayNow)
        {
            KeyMapMap<String, Integer, Long> countMap = new KeyMapMap<>();
            //初始化，设置为null，可以追加保存。
            this.getClass().getDeclaredField("id").set(this, null);
            this.clearCount();
            itemClassList.forEachForcely(itemClass ->
            {

                DataResult<? extends DataModel> dataResult;
                dataResult = DataModel.all(itemClass, a ->
                {


                    a.like("create_datetime", start.get().substring(0, 11));
                    a.orderAscBy("create_datetime");

                });
                dataResult.forEachForcely(item ->
                {
                    handleItem(itemClass, item, countMap);
                });
            });
            this.setDate(start.get());
            ((DataModel) this).save();

            //轮训
            startDate.get().setTime(startDate.get().getTime() + 3600000 * 24);
            start.set(iApp.yyyyMMdd.format(startDate.get()) + " 00:00:00");
        }
    }


    default void handleStatisticsOnEveryHour(Long tick) throws Exception
    {
        DataResult<Class<? extends DataModel>> itemclasslist =new DataResult<>();

        DataResult.castEnumeration(statisticsSumFields().keys()).forEachForcely(a->{
            if(!itemclasslist.contains(a))
            {
                itemclasslist.add(a);
            }
        });
        DataResult.castEnumeration(statisticsCountFields().keys()).forEachForcely(a->{
            if(!itemclasslist.contains(a))
            {
                itemclasslist.add(a);
            }
        });

        handleDimensionals(tick,itemclasslist);

    }

}
