package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.KeyMapMap;
import com.souher.sdk.extend.SystemFunction;
import com.souher.sdk.iApp;
import com.souher.sdk.iString;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface iStatisticsDataCreatorForFourDimensional<E extends DataModel,F extends DataModel>
{
    default KeyMapMap<Class<? extends DataModel>,String,SystemFunction<DataModel,Integer>> statisticsCountFields(E emodel,F fmodel)
    {
        return new KeyMapMap<>();
    }

    default KeyMapMap<Class<? extends DataModel>,String,SystemFunction<DataModel,Long>> statisticsSumFields(E emodel,F fmodel)
    {
        return new KeyMapMap<>();
    }

    default void clearCount(E emodel, F fmodel) throws Exception
    {
        statisticsCountFields(emodel,fmodel).foreach((k, v)->{
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
        statisticsSumFields(emodel,fmodel).foreach((k, v)->{
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

    default void handleItem(Class<? extends DataModel> itemClass,DataModel item, KeyMapMap<String,Integer,Long> countMap,E emodel,F fmodel) throws Exception
    {
        statisticsCountFields(emodel,fmodel).foreach((k, v)->
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
        statisticsSumFields(emodel, fmodel).foreach((k, v)->
        {
            v.forEach((column, function) ->
            {
                try
                {
                    if(!k.equals(itemClass))
                    {
                        return;
                    }
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
    default void handleDimensionals(Long tick,DataResult<Class<? extends DataModel>> itemClassList, Class<E> e, Class<F> f) throws Exception
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
            AtomicBoolean inserted= new AtomicBoolean(false);
            DataModel.all(e).forEachForcely(emodel ->
            {
                DataModel.all(f).forEachForcely(fmodel ->
                {
                    inserted.set(extracted(itemClassList, e, f, start, emodel, fmodel));
                    if(!inserted.get())
                    {
                        return;
                    }
                    Field[] fparentFields=f.getDeclaredFields();
                    boolean hasParent=Arrays.stream(fparentFields).anyMatch(a->a.getName().equals("parent_id"));
                    if(hasParent)
                    {
                        F fmodelTemp=fmodel;
                        while (true)
                        {
                            Integer parent_id= (Integer) f.getDeclaredField("parent_id").get(fmodelTemp);
                            if(parent_id!=null&&parent_id>0)
                            {
                                fmodelTemp= (F) DataModel.first(fmodelTemp.getClass(), a->{
                                    try
                                    {
                                        a.getClass().getDeclaredField("id").set(a,parent_id);
                                    }
                                    catch (Exception illegalAccessException)
                                    {
                                        iApp.error(illegalAccessException);
                                    }
                                });
                                if(fmodelTemp.hasId())
                                {
                                    this.getClass().getDeclaredField(iString.getUnderlineString(f.getSimpleName())+"_is_extra").set(this,true);
                                    this.getClass().getDeclaredField(iString.getUnderlineString(f.getSimpleName()) + "_id").set(this, parent_id);
                                    this.getClass().getDeclaredField("id").set(this,null);
                                    ((DataModel) this).save();
                                    continue;
                                }
                            }
                            break;
                        }
                    }
                    Field[] eparentFields=e.getDeclaredFields();
                    hasParent=Arrays.stream(eparentFields).anyMatch(a->a.getName().equals("parent_id"));
                    if(hasParent)
                    {
                        E emodelTemp=emodel;
                        while (true)
                        {
                            Integer parent_id= (Integer) e.getDeclaredField("parent_id").get(emodelTemp);
                            if(parent_id!=null&&parent_id>0)
                            {
                                emodelTemp= (E) DataModel.first(emodelTemp.getClass(), a->{
                                    try
                                    {
                                        a.getClass().getDeclaredField("id").set(a,parent_id);
                                    }
                                    catch (Exception illegalAccessException)
                                    {
                                        iApp.error(illegalAccessException);
                                    }
                                });
                                if(emodelTemp.hasId())
                                {
                                    this.getClass().getDeclaredField(iString.getUnderlineString(e.getSimpleName())+"_is_extra").set(this,true);
                                    this.getClass().getDeclaredField(iString.getUnderlineString(e.getSimpleName()) + "_id").set(this, parent_id);
                                    this.getClass().getDeclaredField("id").set(this,null);
                                    ((DataModel) this).save();
                                    continue;
                                }
                            }
                            break;
                        }
                    }
                });
            });
            //轮训
            startDate.get().setTime(startDate.get().getTime() + 3600000 * 24);
            start.set(iApp.yyyyMMdd.format(startDate.get()) + " 00:00:00");
        }
    }

    default  boolean extracted(DataResult<Class<? extends DataModel>> itemClassList, Class<E> e, Class<F> f, AtomicReference<String> start, E emodel, F fmodel) throws Exception
    {
        Integer eId = (Integer) e.getDeclaredField("id").get(emodel);
        Integer fId = (Integer) f.getDeclaredField("id").get(fmodel);
        String eFieldName = iString.getUnderlineString(e.getSimpleName()) + "_id";
        String fFieldName = iString.getUnderlineString(f.getSimpleName()) + "_id";
        Field efield = this.getClass().getDeclaredField(eFieldName);
        Field ffield = this.getClass().getDeclaredField(fFieldName);
        efield.set(this, eId);
        ffield.set(this, fId);

        KeyMapMap<String, Integer, Long> countMap = new KeyMapMap<>();
        //初始化，设置为null，可以追加保存。
        this.getClass().getDeclaredField("id").set(this, null);
        this.clearCount(emodel, fmodel);
        itemClassList.forEachForcely(itemClass ->
        {
            Field[] itemClassFields = itemClass.getDeclaredFields();
            Field eItemfield =null;
            Field fItemfield=null;
            for (int i = 0; i < itemClassFields.length; i++)
            {
                if(itemClassFields[i].getName().equals(eFieldName))
                {
                    eItemfield = itemClassFields[i];
                }
                else if(itemClassFields[i].getName().equals(fFieldName))
                {
                    fItemfield = itemClassFields[i];
                }
            }

            DataResult<? extends DataModel> dataResult;
            Field finalFItemfield = fItemfield;
            Field finalEItemfield = eItemfield;
            dataResult = DataModel.all(itemClass, a ->
            {
                try
                {
                    if(finalEItemfield !=null)
                    {
                        finalEItemfield.set(a, eId);
                    }
                    if(finalFItemfield !=null)
                    {
                        finalFItemfield.set(a, fId);
                    }

                    a.like("create_datetime", start.get().substring(0, 11));
                    a.orderAscBy("create_datetime");
                }
                catch (IllegalAccessException illegalAccessException)
                {
                    iApp.error(illegalAccessException);
                }
            });
            dataResult.forEachForcely(item ->
            {
                handleItem(itemClass, item, countMap, emodel, fmodel);
            });
        });
        this.getClass().getDeclaredField(iString.getUnderlineString(e.getSimpleName())+"_is_extra").set(this,false);
        this.getClass().getDeclaredField(iString.getUnderlineString(f.getSimpleName())+"_is_extra").set(this,false);
        this.setDate(start.get());
        this.getClass().getDeclaredField("id").set(this,null);
        ((DataModel) this).first();
        if(!((DataModel) this).hasId())
        {
            ((DataModel) this).save();
            return true;
        }
        return false;
    }

    default void handleStatisticstest() throws Exception
    {
        handleStatisticsOnEveryHour(System.currentTimeMillis());
    }

    default void handleStatisticsOnEveryHour(Long tick) throws Exception
    {
        Type[] genType = getClass().getGenericInterfaces();
        for (int i = 0; i < genType.length; i++)
        {
            if(!(genType[i] instanceof ParameterizedType))
            {
                continue;
            }
            if(!genType[i].getTypeName().startsWith(iStatisticsDataCreatorForFourDimensional.class.getName()))
            {
                continue;
            }
            Type[] params = ((ParameterizedType) genType[i]).getActualTypeArguments();

            Class<E> eClass = (Class<E>)params[0];
            Class<F> fClass = (Class<F>)params[1];
            E emodel=DataModel.first(eClass);
            F fmodel=DataModel.first(fClass);
            DataResult<Class<? extends DataModel>> itemclasslist =new DataResult<>();

            DataResult.castEnumeration(statisticsSumFields(emodel,fmodel).keys()).forEachForcely(a->{
                if(!itemclasslist.contains(a))
                {
                    itemclasslist.add(a);
                }
            });
            DataResult.castEnumeration(statisticsCountFields(emodel,fmodel).keys()).forEachForcely(a->{
                if(!itemclasslist.contains(a))
                {
                    itemclasslist.add(a);
                }
            });
            handleDimensionals(tick,itemclasslist,eClass,fClass);
        }
    }

}
