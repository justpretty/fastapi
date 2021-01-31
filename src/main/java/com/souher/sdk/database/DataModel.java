package com.souher.sdk.database;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.extend.*;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;

import com.souher.sdk.iString;
import com.souher.sdk.interfaces.*;

import org.apache.commons.lang.StringEscapeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DataModel {

    public DataModel(){}

    private static Database database=new Database(iAppConfig.databaseConfig());

    private boolean noIdUpdate=false;
    private boolean noTrigger=false;

    public KeyArrayMap<DatabaseOptions, String> option()
    {
        return option;
    }

    public void setUpdateWithoutId()
    {
        noIdUpdate=true;
    }
    public void setUpdateWithoutTrigger()
    {
        noTrigger=true;
    }
    private KeyArrayMap<DatabaseOptions,String> option = new KeyArrayMap<>();
    private KeyPairMap<String,String,ArrayList> related = new KeyPairMap<>();
    private ArrayList<String> selectFields=new ArrayList<>();

    public ArrayList<FourObject<String, String, ArrayList, KeyMapMap<Class<? extends DataModel>, String, DataModel>>> joined()
    {
        return joined;
    }

    public String cacheTag()
    {
        return "";
    }

    private ArrayList<String> aliasList=new ArrayList();
    private ConcurrentHashMap<String,Class> aliasClassMap=new ConcurrentHashMap<>();
    public ArrayList<String> aliasList()
    {
        return aliasList;
    }

    public ConcurrentHashMap<String, Class> aliasClassMap()
    {
        return aliasClassMap;
    }

    public Boolean noncache=null;

    private ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>> joined=new ArrayList<>();

    private KeyMapMap<Class<? extends DataModel>,String,DataModel> others=new KeyMapMap<>();

    public DataModel getExtraModel(Class<? extends DataModel> cls,String tag)
    {
        return others.get(cls).get(tag);
    }
    public static void truncate(Class<? extends DataModel> cls) throws Exception
    {
        database.truncate(cls);
    }
    public KeyMapMap<Class<? extends DataModel>,String,DataModel> others()
    {
        return others;
    }

    public void setOthers(KeyMapMap<Class<? extends DataModel>,String,DataModel> others)
    {
        this.others=others;
    }

    public ConcurrentHashMap<String,DataModel> getOthers()
    {
        ConcurrentHashMap<String,DataModel> map=new ConcurrentHashMap<>();
        others.foreach((a,b)->{
            map.putAll(b);
        });
        if(map.size()==0)
        {
            return null;
        }
        return map;
    }

    public DataModel join(DataModel model,String myColumn,String modelColumn) throws Exception
    {
        return join("inner",model,myColumn,modelColumn);
    }

    public DataModel leftJoin(DataModel model,String myColumn,String modelColumn) throws Exception
    {
        return join("left",model,myColumn,modelColumn);
    }

    public DataModel rightJoin(DataModel model,String myColumn,String modelColumn) throws Exception
    {
        return join("right",model,myColumn,modelColumn);
    }

    private DataModel join(String preTag,DataModel model,String myColumn,String modelColumn) throws Exception
    {
        String tableName=database.getTableName(model);
        String alias=myColumn;
        if(alias.endsWith("_id"))
        {
            alias=alias.substring(0,alias.length()-3);
        }
        String tag=alias;
        if(model.option().containsKey(DatabaseOptions.table_alias)&&model.option().get(DatabaseOptions.table_alias).size()>0)
        {
            alias=model.option().get(DatabaseOptions.table_alias).get(0);
            tag=alias;
        }
        String myAlias=database.getTableName(this);
        if(option.containsKey(DatabaseOptions.table_alias)&&option.get(DatabaseOptions.table_alias).size()>0)
        {
            myAlias=this.option().get(DatabaseOptions.table_alias).get(0);
        }
        String myTableName=database.getTableName(this);
        String joinString= " "+preTag+" join "+tableName+" "+alias+" on "+myAlias+".`"+myColumn+"`="+tag+".`"+modelColumn+"` ";
        ArrayList parameters=new ArrayList();
        FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>> fourObject=new FourObject<>();
        fourObject.first=joinString;
        fourObject.second=database.getWhereSqlTemplate(model,model.databaseOptions(),new AtomicInteger(),parameters);
        fourObject.third=parameters;
        others.put(model.getClass(),alias,model);
        aliasList.add(alias);
        aliasClassMap.put(alias,model.getClass());
        fourObject.fourth=others;
        joined.add(fourObject);
        //iApp.debug("before:"+oo.toString());
        return this;
    }



    public FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> databaseOptions()
    {
        return FourObject.Of(option, related,selectFields,joined);
    }
    public FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> databaseOptions(KeyArrayMap<DatabaseOptions,String> option)
    {
        return FourObject.Of(option, related,selectFields,joined);
    }
    public static FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> getDefaultDatabaseOptions(KeyArrayMap<DatabaseOptions,String> option)
    {
        return FourObject.Of(option,new KeyPairMap<String,String,ArrayList>(),new ArrayList<String>(),new ArrayList<>());
    }
    public boolean hasId() throws Exception
    {
        return id()!=null;
    }
    public Object id() throws Exception
    {
        Object id=this.getClass().getDeclaredField("id").get(this);
        return id;
    }

    public DataModel select(String ...fields)
    {
        selectFields.clear();
        for (int i = 0; i < fields.length; i++)
        {
            selectFields.add(fields[i]);
        }
        return this;
    }

    public static <T extends DataModel> DataResult<T> all(Class<T> cls) throws Exception {
        return all(cls, null);
    }

    public static Database getDatabase()
    {
        return database;
    }

    public static <T extends DataModel> DataResult<T> all(Class<T> cls, Consumer<T> consumer) throws Exception {

        T b=cls.newInstance();
        if(consumer!=null)
        {
            consumer.accept(b);
        }
        return getSelect(b, b.databaseOptions());
    }

    private static <T extends DataModel> DataResult<T> getSelect(T b, FourObject<KeyArrayMap<DatabaseOptions, String>, KeyPairMap<String, String, ArrayList>, ArrayList<String>, ArrayList<FourObject<String, String, ArrayList, KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> databaseOptions) throws Exception
    {
        //iApp.debug("CLASS:"+b.getClass().getSimpleName());
        if(iHasForeignKeys.class.isAssignableFrom(b.getClass()))
        {
            ((iHasForeignKeys) b).handleForeignKeys().forEachForcely(a->{
                b.leftJoin(a.destination,a.sourceColumn,a.destinationColumn);
            });
        }
//        iApp.debug("others::::"+b.others());
        DataResult<T> result=database.select(b, b.databaseOptions());

        return result;
    }

    public int save() throws Exception {
        iApp.debug(this.toString(),"DataModel.save");
        Field[] fields=this.getClass().getDeclaredFields();
        int id=0;
        for(Field field: fields)
        {
            if(field.getName().equals("id")&&field.get(this)!=null)
            {
                id= (int) field.get(this);
                break;
            }
        }
        if(id>0||noIdUpdate)
        {
            int num= database.update(this,null,databaseOptions());
            if(num>0&&!noTrigger)
            {
                InterfaceExecutor.Current.executor(iOnDataUpdated.class, this);
                InterfaceExecutor.Current.executor(iOnDataSaved.class, this);
            }
            return num;
        }

        InterfaceExecutor.Current.check(iOnDataInserting.class,this);

        int resultId=database.insert(this);
        this.getClass().getDeclaredField("id").set(this, resultId);
        if(resultId>0&&!noTrigger)
        {
            InterfaceExecutor.Current.executor(iOnDataInserted.class, this);
            InterfaceExecutor.Current.executor(iOnDataSaved.class, this);
        }
        return resultId;
    }

     public static <T extends DataModel> void delete(Class<T> cls, SystemConsumer<T> where) throws Exception
    {
        T instance=cls.newInstance();
        if(where!=null)
        {
            where.accept(instance);
        }
        instance.selectSimilarModels(cls).forEachForcely(a->{
            if(a.hasId())
            {
                a.delete();

            }
        });
    }
    public static <T extends DataModel> void update(Class<T> cls, SystemConsumer<T> where,SystemConsumer<T> set) throws Exception
    {
        T instance=cls.newInstance();
        if(where!=null)
        {
            where.accept(instance);
        }
        instance.selectSimilarModels(cls).forEachForcely(a->{
            if(set!=null)
            {
                set.accept(a);
            }
            a.save();

        });
    }
    public static <T extends DataModel> int count(Class<T> cls, SystemConsumer<T> where) throws Exception
    {
        T instance=cls.newInstance();
        if(where!=null)
        {
            where.accept(instance);
        }
        instance.selectCount();
        instance.first();
        if(!instance.hasId())
        {
            throw new Exception("sql ERROR count not fetch!");
        }
        String id=instance.getClass().getDeclaredField("id").get(instance).toString();
        iApp.debug("count:"+id);
        return Integer.parseInt(id);
    }

    public  DataResult selectSimilarModels() throws Exception {
        return getSelect(this, this.databaseOptions());
    }
    public <T extends DataModel> DataResult<T> selectSimilarModels(Class<T> cls) throws Exception {
        return (DataResult<T>) getSelect(this, this.databaseOptions());
    }

    public void refresh() throws Exception {
        KeyArrayMap<DatabaseOptions,String> option = new KeyArrayMap<DatabaseOptions,String>();
        option.remove(DatabaseOptions.limit);
        option.append(DatabaseOptions.limit,"1");
        Field[] fields=this.getClass().getDeclaredFields();
        for(Field field:fields)
        {
//            iApp.print(field.getName());
            if(!field.getName().equals("id"))
            {
                field.set(this,null);
            }
        }
        iApp.debug(this.toString());
        this.option=option;
        getSelect(this, databaseOptions(option));
    }

    public int delete() throws Exception {
        int num=database.delete(this);
        if(num>0)
        {
            DataModel instance=JSONObject.parseObject(this.toString(),this.getClass());
            if(iRedisModel.class.isAssignableFrom(this.getClass()))
            {
                ((iRedisModel)this).removeFromRedis();
            }
            InterfaceExecutor.Current.executor(iOnDataDeleted.class, instance);
            this.getClass().getDeclaredField("id").set(this, null);
        }
        return num;
    }

    public DataModel orderDescBy(String columnName)
    {
        this.option.append(DatabaseOptions.orders,columnName+"#desc");
        return this;
    }
    public DataModel orderAscBy(String columnName)
    {
        this.option.append(DatabaseOptions.orders,columnName+"#asc");
        return this;
    }
    public DataModel orderByRandom()
    {
        this.option.append(DatabaseOptions.orderrandom,"true");
        return this;
    }

    public DataModel not(String columnName,String value)
    {
        this.option.append(DatabaseOptions.raw,"` #TABLE#."+columnName+"`!='"+escape(value)+"'");
        return this;
    }
    public DataModel isNotNullOrEmpty(String columnName)
    {
        this.option.append(DatabaseOptions.isnotnullorempty,columnName);
        return this;
    }
    public DataModel isNotNull(String columnName)
    {
        this.option.append(DatabaseOptions.isnotnull,columnName);
        return this;
    }
    public DataModel isNull(String columnName)
    {
        this.option.append(DatabaseOptions.isnull,columnName);
        return this;
    }
    public DataModel bigThan(String columnName,long value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"`>"+value+"");
        return this;
    }

    public DataModel bigThanColumn(String columnName,String otherColumn)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"`>#TABLE#.`"+otherColumn+"`");
        return this;
    }

    public DataModel bigThan(String columnName,String value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"`>'"+value+"'");
        return this;
    }
    public DataModel smallThan(String columnName,long value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"`<"+value+"");
        return this;
    }
    private String escape(String a)
    {
        return a.replaceAll("'","\\'");
    }
    private String likeescape(String a)
    {
        return escape(a).replaceAll("_","\\_")
                .replaceAll("%","\\%");
    }
    public DataModel like(String columnName,String value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"` like '%"+likeescape(value) +"%'");
        return this;
    }

    public DataModel startWith(String columnName,String value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"` like '"+likeescape(value)+"%'");
        return this;
    }

    public DataModel endWith(String columnName,String value)
    {
        this.option.append(DatabaseOptions.raw," #TABLE#.`"+columnName+"` like '%"+likeescape(value)+"'");
        return this;
    }

    public DataModel between(String columnName,long small,long big)
    {
        String tableName= getDatabase().getTableName(this);
        this.option.append(DatabaseOptions.between," #TABLE#.`"+columnName+"` between "+small+" and "+big+" ");
        return this;
    }
    public DataModel between(String columnName,String small,String big)
    {
        String tableName= getDatabase().getTableName(this);
        this.option.append(DatabaseOptions.between," #TABLE#.`"+columnName+"` between '"+escape(small)+"' and '"+escape(big)+"' ");
        return this;
    }
    public DataModel selectCount()
    {
        this.option.append(DatabaseOptions.count,"id");
        return this;
    }
    public DataModel groupBy(String columnName)
    {
        this.option.append(DatabaseOptions.group,columnName);
        return this;
    }

    public DataModel in(String columnName,DataModel model) throws Exception
    {
        Pair<String,ArrayList> pair=new Pair<>();
        ArrayList parameters=new ArrayList();
        String sqlTemplate=database.getSqlTemplate(model,model.databaseOptions(),new AtomicInteger(),parameters);
        this.related.append(columnName,"in ("+sqlTemplate+")",parameters);
        return this;
    }

    public DataModel in(String columnName,ArrayList<String> list) throws Exception
    {
        if(list.size()==0)
        {
            throw new Exception("setIn list is empty!");
        }
        StringBuilder sb = new StringBuilder();
        String tableName= getDatabase().getTableName(this);
        sb.append(" #TABLE#.`"+columnName+"` in ('");
        list.forEach(a->{
            sb.append(StringEscapeUtils.escapeSql(a)).append("','");
        });
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append(") ");
        this.option.append(DatabaseOptions.in,sb.toString());
        return this;
    }


    public DataModel notIn(String columnName,DataModel model) throws Exception
    {
        Pair<String,ArrayList> pair=new Pair<>();
        ArrayList parameters=new ArrayList();
        String sqlTemplate=database.getSqlTemplate(model,model.databaseOptions(),new AtomicInteger(),parameters);
        this.related.append(columnName,"not in ("+sqlTemplate+")",parameters);
        return this;
    }

    public DataModel not(String columnName,DataModel model) throws Exception
    {
        Pair<String,ArrayList> pair=new Pair<>();
        ArrayList parameters=new ArrayList();
        String sqlTemplate=database.getSqlTemplate(model,model.databaseOptions(),new AtomicInteger(),parameters);
        this.related.append(columnName,"!= ("+sqlTemplate+")",parameters);
        return this;
    }



    public DataModel notIn(String columnName,ArrayList<String> list) throws Exception
    {
        if(list.size()==0)
        {
            throw new Exception("setnotIn list is empty!");
        }
        StringBuilder sb = new StringBuilder();
        String tableName= getDatabase().getTableName(this);
        sb.append(" #TABLE#.`"+columnName+"` not in ('");
        list.forEach(a->{
            sb.append(StringEscapeUtils.escapeSql(a)).append("','");
        });
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append(") ");
        this.option.append(DatabaseOptions.notin,sb.toString());
        return this;
    }

    public DataModel limit(Long offset, Long limit)
    {
        this.option.remove(DatabaseOptions.limit);
        this.option.append(DatabaseOptions.limit,offset.toString());
        this.option.append(DatabaseOptions.limit,limit.toString());
        return this;
    }
    public DataModel limit(Long limit)
    {
        this.option.remove(DatabaseOptions.limit);
        this.option.append(DatabaseOptions.limit,limit.toString());
        return this;
    }

    public DataModel increase(String columnName) throws Exception{
        this.option.append(DatabaseOptions.increase,columnName);
        return this;
    }
    public DataModel increase(String columnName,int value) throws Exception{
        this.option.append(DatabaseOptions.increase,columnName);
        this.option.append(DatabaseOptions.increasevalue, String.valueOf(value));
        return this;
    }

    public DataModel decrease(String columnName) throws Exception{
        this.option.append(DatabaseOptions.decrease,columnName);
        return this;
    }

    public DataModel decrease(String columnName,int value) throws Exception{
        this.option.append(DatabaseOptions.decrease,columnName);
        this.option.append(DatabaseOptions.decreasevalue,String.valueOf(value));
        return this;
    }

    public void first() throws Exception {
        Class<? extends DataModel> cls=this.getClass();
        if(iRedisModel.class.isAssignableFrom(cls)&&(noncache==null|| !noncache))
        {
            boolean exists=((iRedisModel)this).fillFromRedis();
            if(exists)
            {
                iApp.debug(this.getClass().getSimpleName()+".first"," Got In Cache:"+JSON.toJSONString(this));
                return;
            }
        }
        this.option.remove(DatabaseOptions.limit);
        this.option.append(DatabaseOptions.limit,"1");
        getSelect(this, databaseOptions());
        if(iRedisModel.class.isAssignableFrom(cls))
        {
            if(this.hasId())
            {
                iApp.debug(this.getClass().getSimpleName()+".first"," Saved To Cache:"+JSON.toJSONString(this));
                ((iRedisModel)this).saveToRedis();
            }
        }
    }
    public static <T extends DataModel> T first(Class<T> cls) throws Exception {
        return first(cls,null);
    }

    public static <T extends DataModel> T first(Class<T> cls,Consumer<T> consumer) throws Exception {
        T a=cls.newInstance();
        if(consumer!=null)
        {
            consumer.accept(a);
        }
        a.first();
        return a;
    }

    public void print()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n-----------------------\n");
        try
        {
            Field[]   fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.get(this) == null) {
                    sb.append(field.getName()).append("\t:\tnull\n");
                }
                else
                {
                    sb.append(field.getName()).append("\t:\t").append(field.get(this).toString()).append('\n');
                }
            }
            sb.append("-----------------------\n");
            iApp.debug(sb.toString());
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }


    @Override
    public String toString() {
        JSONObject jsonObject=new JSONObject();
        try
        {
            Field[]   fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }
                jsonObject.put(field.getName(),field.get(this));
            }
            return JSON.toJSONString(jsonObject,SerializerFeature.WRITE_MAP_NULL_FEATURES);
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
        return "";
    }

    public String toStringWithOutNull() {
        JSONObject jsonObject=new JSONObject();
        try
        {
            Field[]   fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }
                if (field.get(this) == null) {
                    jsonObject.put(field.getName(),null);
                }
                else
                {
                    jsonObject.put(field.getName(),field.get(this).toString());
                }
            }
            return JSON.toJSONString(jsonObject);
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
        return "";
    }
//    public DataResult<DataModelRelation> WithParentsWithTag(DataModel destinationClass,
//                                                     String alias,
//                                                     String sourceColumn,
//                                                     String destinationColumn,
//                                                     Object... args)
//    {
//        DataResult<DataModelRelation> result=new DataResult<>();
//        DataModelRelation relation=new DataModelRelation();
//        relation.source = this;
//        relation.destination = destinationClass;
//        relation.destinationAlias=alias;
//        relation.sourceColumn = sourceColumn;
//        relation.destinationColumn = destinationColumn;
//        result.add(relation);
//        for (int i = 0; i < args.length; i+=4)
//        {
//            relation=new DataModelRelation();
//            relation.source = this;
//            relation.destination = (DataModel) args[i];
//            relation.destinationAlias=args[i+1].toString();
//            relation.sourceColumn =  args[i+2].toString();
//            relation.destinationColumn =args[i+3].toString();
//            result.add(relation);
//        }
//        return result;
//    }



    public DataResult<DataModelRelation> WithParents(DataModel destinationClass,
                                                     String sourceColumn,
                                                     String destinationColumn,
                                                     Object... args)
    {
        DataResult<DataModelRelation> result=new DataResult<>();
        DataModelRelation relation=new DataModelRelation();
        relation.source = this;
        relation.destination = destinationClass;
        relation.sourceColumn = sourceColumn;
        relation.destinationColumn = destinationColumn;
        result.add(relation);
        for (int i = 0; i < args.length; i+=3)
        {
            relation=new DataModelRelation();
            relation.source = this;
            relation.destination = (DataModel) args[i];
            relation.sourceColumn =  args[i+1].toString();
            relation.destinationColumn =args[i+2].toString();
            result.add(relation);
        }
        return result;
    }

    public void clearall() throws Exception
    {
        Field[]   fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
            {
                continue;
            }
            if (field.get(this) == null) {
                continue;
            }
            field.set(this,null);
        }
    }
}
