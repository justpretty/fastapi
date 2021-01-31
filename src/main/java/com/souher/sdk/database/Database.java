package com.souher.sdk.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.reflect.TypeToken;

import com.souher.sdk.extend.*;
import com.souher.sdk.extend.FourObject;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iDatabaseConfig;
import com.souher.sdk.interfaces.iHasForeignKeys;

import java.lang.reflect.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Database {

    private DruidDataSource dataSource;
    private iDatabaseConfig config;
    private static ConcurrentHashMap<String,String> hasCheckedCache=new ConcurrentHashMap<>();

    public DruidDataSource dataSource()
    {
        return dataSource;
    }
    public Database(iDatabaseConfig config)
    {
        this.config=config;
        dataSource=new DruidDataSource();
        String url = "jdbc:mysql://"+ config.host() +":"+ config.port() +"/"+ config.database() +"?serverTimezone=Asia/Shanghai&autoReconnect=true&zeroDateTimeBehavior=convertToNull&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true";
        try
        {
            dataSource.setUrl(url);
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setMinIdle(config.minPoolSize());
            dataSource.setMaxActive(config.maxPoolSize());
            dataSource.setInitialSize(config.initPoolSize());

            dataSource.setUsername(config.user());
            dataSource.setPassword(config.password());
            dataSource.setRemoveAbandoned(false);
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
            dataSource.setMinEvictableIdleTimeMillis(300000);
            dataSource.setBreakAfterAcquireFailure(false);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
            dataSource.setMaxWait(60000);
            dataSource.setAsyncInit(true);
            dataSource.setPoolPreparedStatements(true);
            dataSource.setMaxOpenPreparedStatements(100);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(50);
            dataSource.setKeepAlive(true);
            dataSource.setConnectionInitSqls(Collections.singletonList("SET names utf8mb4;"));

        } catch (Exception e) {
            iApp.error(e);
        }
    }

    public <T extends DataModel> DataResult<T> select (T where, FourObject<KeyArrayMap<DatabaseOptions,String>, KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList) throws Exception {
        return CheckExecute(where, (connection, tableName, model, modelType) ->
                Database.this.selectModel(connection,tableName,model,where,optionList)
        );
    }

    public long truncate(Class<? extends DataModel> cls) throws Exception
    {
        String clsName=cls.getSimpleName();
        String tableName=getPrefixedLinedString(clsName);
        Connection connection=null;
        try {
            connection=dataSource.getConnection();
            String sql="truncate `"+tableName+"`";
            PreparedStatement preparedStatement = createStatement(sql, new DataResult<>(), connection);
            long executeRowNum = preparedStatement.executeUpdate();
            return executeRowNum;
        }finally {
            if(connection!=null)
            {
                try {
                    connection.close();
                }
                catch (Exception e){}
            }
        }
    }

    public <T> String getSqlTemplate (T where, FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList,AtomicInteger limitRefer,ArrayList parameters) throws Exception {
        return CheckExecute(where, (connection, tableName, model, modelType) ->
                createSqlTemplate(tableName,model,optionList,limitRefer,parameters)
        );
    }

    public <T> String getWhereSqlTemplate (T where, FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList, AtomicInteger limitRefer, ArrayList parameters) throws Exception {
        return CheckExecute(where, (connection, tableName, model, modelType) ->
                createWhereSqlTemplate(tableName,model,optionList,limitRefer,parameters)
        );
    }

    public int update(Object value ,Object where,FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList) throws Exception {
        ConcurrentHashMap whereModel = new ConcurrentHashMap();
        Field[] fields=value.getClass().getDeclaredFields();
        for(Field field: fields)
        {
            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
            {
                continue;
            }
            if(where!=null && field.get(where)!=null) {
                whereModel.put(field.getName(), field.get(where));
            }
        }
        return CheckExecute(value, (connection, tableName, model, modelType) ->
                Database.this.updateModel(connection,tableName,model,whereModel,optionList)
        );
    }
    public int insert(Object value) throws Exception {
        return CheckExecute(value, (connection, tableName, model, modelType) -> Database.this.insertModel(connection,tableName,model));
    }
    public int delete(Object value) throws Exception {
        return CheckExecute(value, (connection, tableName, model, modelType) -> Database.this.deleteModel(connection,tableName,model));
    }


    private interface ICheck<E>
    {
        E afterCheck(Connection connection,String tableName,ConcurrentHashMap model,ConcurrentHashMap modelType) throws Exception;
    }

    private <E extends DataModel> DataResult<E> selectModel(Connection connection, String tableName, ConcurrentHashMap whereModel,E where,FourObject<KeyArrayMap<DatabaseOptions,String>, KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList) throws Exception {

        AtomicInteger limitRefer=new AtomicInteger();
        ArrayList parameters = new ArrayList();
        String sql = createSqlTemplate(tableName, whereModel, optionList, limitRefer, parameters);

        Integer limit=limitRefer.get();
        PreparedStatement preparedStatement = createStatement(sql, parameters, connection);
        ResultSet rs = preparedStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int colum = metaData.getColumnCount();
        ArrayList<String> columnNames = new ArrayList<String>();
        KeyArrayMap<String,Integer> columIndexMaps=new KeyArrayMap<>();
        for (int i = 1; i <= colum; i++) {
            String columName = metaData.getColumnLabel(i);
            columIndexMaps.append(columName,i);
            columnNames.add(columName);
        }
        //iApp.debug("4444:"+columIndexMaps.toString());
        Constructor constructor = where.getClass().getConstructor();
        Field[] fields = where.getClass().getDeclaredFields();
        ArrayList<Field> fieldList = new ArrayList<Field>();
        for (Field field : fields) {
            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
            {
                continue;
            }
            if (columnNames.contains(field.getName())) {
                fieldList.add(field);
            }
        }
        DataResult<E> list = new DataResult<E>();

        if(limit!=null && limit.equals(1)) {
            if (rs.next()) {
                KeyArrayMap<String,Integer> columIndexMap=columIndexMaps.clone();
                for (Field field : fieldList) {
                    if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                    {
                        continue;
                    }
                    String value = rs.getString(field.getName());
                    setFieldValue(where,field,value);
                    if(columIndexMap.containsKey(field.getName()))
                    {
                        columIndexMap.get(field.getName()).remove(0);
                    }

                    if(field.getName().equals("id")&&value!=null)
                    {
                        list.addId(where.getClass(),Integer.parseInt(value));
                    }
                }


                if(where.others().size()>0)
                {
                    handleOptionList(list,where,where, rs, columIndexMap);
                }
            } else {
                where.getClass().getDeclaredField("id").set(where, null);
            }
            list.add(where);
            return list;
        }
        else
        {
            while (rs.next()) {
                KeyArrayMap<String,Integer> columIndexMap=columIndexMaps.clone();
                //iApp.debug("666666:"+columIndexMap.toString());
                E obj = (E)constructor.newInstance();
                for (Field field : fieldList) {
                    if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                    {
                        continue;
                    }
                    String value = rs.getString(field.getName());
                    setFieldValue(obj,field,value);
                    if(columIndexMap.containsKey(field.getName()))
                    {
                        columIndexMap.get(field.getName()).remove(0);
                    }
                    if(field.getName().equals("id")&&value!=null)
                    {
                        list.addId(where.getClass(),Integer.parseInt(value));
                    }
                }
                if(where.others().size()>0)
                {
                    handleOptionList(list,where,obj, rs, columIndexMap);
                }
                list.add(obj);
            }
            return list;
        }
    }

    private <E extends DataModel> void handleOptionList(DataResult<E> list,E where,E obj, ResultSet rs, KeyArrayMap<String, Integer> columIndexMap) throws Exception
    {
            if(!iHasForeignKeys.class.isAssignableFrom(where.getClass()))
            {
                return;
            }
            DataResult.castList(where.aliasList()).forEachForcely(c->{
                Class<E> b=where.aliasClassMap().get(c);
                DataModel mmodel = b.newInstance();
                for (Field field : b.getDeclaredFields())
                {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
                    {
                        continue;
                    }
                    int tag=0;
                    if(columIndexMap.containsKey(field.getName())
                            &&columIndexMap.get(field.getName()).size()>0)
                    {
                        tag= columIndexMap.get(field.getName()).remove(0);
                        String value = rs.getString(tag);
                        setFieldValue(mmodel, field, value);
                        if(field.getName().equals("id")&&value!=null)
                        {
                            list.addId(mmodel.getClass(),Integer.parseInt(value));
                        }
                    }
                }
                obj.others().put(b,c,mmodel);
            });

    }

    private String createWhereSqlTemplate(String tableName, ConcurrentHashMap whereModel, FourObject<KeyArrayMap<DatabaseOptions,String>, KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList , AtomicInteger limitRefer, ArrayList parameters)
    {
        StringBuilder sb= new StringBuilder();
        KeyArrayMap<DatabaseOptions,String> option=optionList.first;
        String alias="";
        String tag=tableName;
        if(option.containsKey(DatabaseOptions.table_alias)&&option.get(DatabaseOptions.table_alias).size()>0)
        {
            alias=option.get(DatabaseOptions.table_alias).get(0);
            tag=alias;
        }

        Integer limit2= handlerWhere( tag,whereModel, optionList, sb, parameters);
        if(limit2!=null)
        {
            limitRefer.set(limit2);
        }
        String sql=sb.toString();
        return sql;
    }
    private String createSqlTemplate(String tableName, ConcurrentHashMap whereModel, FourObject<KeyArrayMap<DatabaseOptions,String>, KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList , AtomicInteger limitRefer, ArrayList parameters)
    {
        StringBuilder sb= new StringBuilder();
        KeyArrayMap<DatabaseOptions,String> option=optionList.first;
        String alias="";
        String tag=tableName;
        if(option.containsKey(DatabaseOptions.table_alias)&&option.get(DatabaseOptions.table_alias).size()>0)
        {
            alias=option.get(DatabaseOptions.table_alias).get(0);
            tag=alias;
        }
        if(option.containsKey(DatabaseOptions.count))
        {
            sb.append("select count(*) id ");
            if(option.containsKey(DatabaseOptions.group))
            {
                option.get(DatabaseOptions.group).forEach(a->sb.append(",`").append(a).append("` "));
            }
            sb.append(" from `").append(tableName).append("` ").append(alias).append(" ");
            optionList.fourth.forEach(a->{
                sb.append(" ").append(a.first).append(" ");
            });

            sb.append(" where 1=1 ");
        }
        else if(optionList.third.size()>0)
        {
            StringBuilder sbFieldString=new StringBuilder();
            optionList.third.forEach(a->{
                if(a.contains(" "))
                {
                    sbFieldString.append(a).append(",");
                    return;
                }
                sbFieldString.append("`").append(a).append("`,");
            });
            sbFieldString.deleteCharAt(sbFieldString.length()-1);
            String fieldString=sbFieldString.toString();
            sb.append("select "+fieldString+" from `").append(tableName).append("` ").append(alias).append(" ");
            optionList.fourth.forEach(a->{
                sb.append(" ").append(a.first).append(" ");
            });
            sb.append(" where 1=1 ");
        }
        else
        {
            sb.append("select * from `").append(tableName).append("` ").append(alias).append(" ");
            optionList.fourth.forEach(a->{
                sb.append(" ").append(a.first).append(" ");
            });
            sb.append(" where 1=1 ");
        }

        Integer limit2= handlerWhere( tag,whereModel, optionList, sb, parameters);
        if(limit2!=null)
        {
            limitRefer.set(limit2);
        }
        String sql=sb.toString();
        return sql;
    }

    private int updateModel(Connection connection,String tableName, ConcurrentHashMap valueModel,ConcurrentHashMap whereModel,FourObject<KeyArrayMap<DatabaseOptions,String>,KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList) throws SQLException {
        StringBuilder sb = new StringBuilder();
        KeyArrayMap<DatabaseOptions,String> option= optionList.first;
        ArrayList list = new ArrayList();
        sb.append("update `").append(tableName).append("` set ");
        int id=0;
        for(Object key:valueModel.keySet())
        {
            Object value=valueModel.get(key);
            if(key.toString().equals("id"))
            {
                id= Integer.valueOf(value.toString());
            }
            if(value==null)
            {
                continue;
            }
            sb.append("`").append(key).append("`=? ,");

            list.add(value);
        }
        if(option!=null&&option.size()>0)
        {
            if(option.containsKey(DatabaseOptions.increase))
            {
                ArrayList<String> values=new ArrayList<>();
                if(option.containsKey(DatabaseOptions.increasevalue))
                {
                    values=option.get(DatabaseOptions.increasevalue);
                }
                ArrayList<String> increaselist=option.get(DatabaseOptions.increase);
                for(int i=0;i<increaselist.size();i++)
                {
                    String columnName=increaselist.get(i);
                    int value=1;
                    if(values.size()>i)
                    {
                        value= Integer.parseInt(values.get(i));
                    }
                    sb.append("`")
                            .append(columnName)
                            .append("`=`")
                            .append(columnName)
                            .append("`+")
                            .append(value)
                            .append(" ,");
                }
            }
            else if(option.containsKey(DatabaseOptions.decrease))
            {
                ArrayList<String> values=new ArrayList<>();
                if(option.containsKey(DatabaseOptions.decreasevalue))
                {
                    values=option.get(DatabaseOptions.decreasevalue);
                }
                ArrayList<String> increaselist=option.get(DatabaseOptions.decrease);
                for(int i=0;i<increaselist.size();i++)
                {
                    String columnName=increaselist.get(i);
                    int value=1;
                    if(values.size()>i)
                    {
                        value= Integer.parseInt(values.get(i));
                    }
                    sb.append("`")
                            .append(columnName)
                            .append("`=`")
                            .append(columnName)
                            .append("`-")
                            .append(value)
                            .append(" ,");
                }
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(" where 1=1 ");
        if(whereModel!=null && whereModel.size()>0)
        {
            handlerWhere(tableName,whereModel,optionList, sb, list);
        }
        else if(id>0)
        {
            sb.append(" and id=? ");
            list.add(id);
        }

        PreparedStatement preparedStatement = createStatement(sb.toString(),list, connection);
        return preparedStatement.executeUpdate();
    }

    private Integer handlerWhere(String tableName,ConcurrentHashMap whereModel, FourObject<KeyArrayMap<DatabaseOptions,String>, KeyPairMap<String,String,ArrayList>,ArrayList<String>,ArrayList<FourObject<String,String,ArrayList,KeyMapMap<Class<? extends DataModel>,String,DataModel>>>> optionList, StringBuilder sb, ArrayList parameters) {
        ArrayList<String> notList = new ArrayList<>();
        ArrayList<String> bigList = new ArrayList<>();
        ArrayList<String> smallList = new ArrayList<>();
        ArrayList<String> likeList = new ArrayList<>();
        ArrayList<String> regexList = new ArrayList<>();

        KeyArrayMap<DatabaseOptions,String> option=optionList.first;
        KeyPairMap<String,String,ArrayList> related= optionList.second;
        if(related==null)
        {
            related=new KeyPairMap<>();
        }
        if(option!=null&&option.size()>0)
        {
            if(option.containsKey(DatabaseOptions.raw))
            {
                option.get(DatabaseOptions.raw).forEach(a->{
                    sb.append(" and "+a.replaceAll("#TABLE#","`"+tableName+"`")+" ");
                });
            }
            if(option.containsKey(DatabaseOptions.not))
            {
                notList=option.get(DatabaseOptions.not);
            }
            if(option.containsKey(DatabaseOptions.like))
            {
                likeList=option.get(DatabaseOptions.like);
            }
            if(option.containsKey(DatabaseOptions.regex))
            {
                regexList=option.get(DatabaseOptions.regex);
            }
            if(option.containsKey(DatabaseOptions.big))
            {
                bigList=option.get(DatabaseOptions.big);
            }
            if(option.containsKey(DatabaseOptions.small))
            {
                smallList=option.get(DatabaseOptions.small);
            }
            if(option.containsKey(DatabaseOptions.isnull))
            {
                option.get(DatabaseOptions.isnull).forEach(a->{
                    sb.append(" and "+tableName+".`").append(a).append("` is null ");
                });
            }
            if(option.containsKey(DatabaseOptions.isnotnull))
            {
                option.get(DatabaseOptions.isnotnull).forEach(a->{
                    sb.append(" and "+tableName+".`").append(a).append("` is not null ");
                });
            }
            if(option.containsKey(DatabaseOptions.isnotnullorempty))
            {
                option.get(DatabaseOptions.isnotnullorempty).forEach(a->{
                    sb.append(" and "+tableName+".`").append(a).append("` is not null ").append(" and "+tableName+".`").append(a).append("` !='' ");
                });
            }
            if(option.containsKey(DatabaseOptions.between))
            {
                option.get(DatabaseOptions.between).forEach(a->{
                    sb.append(" and ").append(a.replaceAll("#TABLE#","`"+tableName+"`"));
                });
            }
            if(option.containsKey(DatabaseOptions.in))
            {
                option.get(DatabaseOptions.in).forEach(a->{
                    sb.append(" and ").append(a.replaceAll("#TABLE#","`"+tableName+"`"));
                });
            }
            if(option.containsKey(DatabaseOptions.notin))
            {
                option.get(DatabaseOptions.notin).forEach(a->{
                    sb.append(" and ").append(a.replaceAll("#TABLE#","`"+tableName+"`"));
                });
            }
        }

        for(Object key:whereModel.keySet())
        {
            if(notList.contains(key))
            {
                if(likeList.contains(key))
                {
                    sb.append(" and "+tableName+".`").append(key).append("` not like ?");
                }
                else {
                    sb.append(" and "+tableName+".`").append(key).append("`!=?");
                }
            }
            else if(bigList.contains(key))
            {
                sb.append(" and "+tableName+".`").append(key).append("`>?");
            }
            else if(smallList.contains(key))
            {
                sb.append(" and "+tableName+".`").append(key).append("`<?");
            }
            else
            {
                if(likeList.contains(key))
                {
                    sb.append(" and "+tableName+".`").append(key).append("` like ?");
                }
                else if(regexList.contains(key))
                {
                    sb.append(" and "+tableName+".`").append(key).append("` REGEXP ?");
                }
                else {
                    sb.append(" and "+tableName+".`").append(key).append("`=?");
                }
            }
            if(likeList.contains(key))
            {
                parameters.add('%'+whereModel.get(key).toString()+'%');
            }
            else {
                parameters.add(whereModel.get(key));
            }
        }

        related.foreach((columnName,pair)->{
            sb.append(" and "+tableName+".`").append(columnName).append("` ").append(pair.one).append(" ");
            parameters.addAll(pair.theother);
        });

        optionList.fourth.forEach(a->{
            sb.append(" ").append(a.second).append(" ");
            parameters.addAll(a.third);
        });


        if(option.containsKey(DatabaseOptions.group))
        {
            sb.append(" group by ");
            option.get(DatabaseOptions.group).forEach(a->{
                sb.append(" "+tableName+".`").append(a).append("` ,");
            });
            sb.deleteCharAt(sb.length()-1);
        }

        if(option!=null&&option.size()>0)
        {

            if(option.containsKey(DatabaseOptions.orderrandom))
            {
                sb.append(" rand(),");
            }
            else if(option.containsKey(DatabaseOptions.orders))
            {
                sb.append(" order by ");
                option.get(DatabaseOptions.orders).forEach(a->{
                    String[] bb=a.split("#");
                    if(bb[0].contains("."))
                    {
                        sb.append(" ").append(bb[0]).append(" "+bb[1]+",");
                    }
                    else
                    {
                        sb.append(" "+tableName+".`").append(bb[0]).append("` "+bb[1]+",");
                    }
                });
            }
            if(option.containsKey(DatabaseOptions.orders)
                    ||option.containsKey(DatabaseOptions.orderrandom)
            )
            {
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append(" ");
        }
        Integer limit=null;
        if(option.containsKey(DatabaseOptions.limit))
        {
            ArrayList<String> limitArray=option.get(DatabaseOptions.limit);
            if(limitArray.size()==1)
            {
                limit=Integer.parseInt(limitArray.get(0));
                sb.append(" limit ").append(limit);
            }
            else if(limitArray.size()==2)
            {
                limit=Integer.parseInt(limitArray.get(1));
                sb.append(" limit ").append(limitArray.get(0)).append(",").append(limit);
            }
        }
        return limit;
    }

    private int insertModel(Connection connection,String tableName, ConcurrentHashMap model) throws SQLException {
        StringBuilder sb = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        ArrayList list = new ArrayList();
        sb.append("insert ignore into `").append(tableName).append("` (");

        for(Object key:model.keySet())
        {
            Object value=model.get(key);
            if(value==null)
            {
                continue;
            }
            sb.append("`").append(key).append("`,");
            valueBuilder.append("?,");

            list.add(value);
        }
        sb.deleteCharAt(sb.length()-1);
        valueBuilder.deleteCharAt(valueBuilder.length()-1);
        sb.append(") values (").append(valueBuilder).append(")");

        PreparedStatement preparedStatement = createStatement(sb.toString(),list, connection);
        preparedStatement.executeUpdate();
        ResultSet rs =null;
        rs = preparedStatement.getGeneratedKeys();
        if(rs.next())
        {
            int id=rs.getInt(1);
            return id;
        }
        return 0;
    }

    private int deleteModel(Connection connection,String tableName, ConcurrentHashMap model) throws SQLException {
        StringBuilder sb = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        ArrayList list = new ArrayList();
        sb.append("delete from `").append(tableName).append("` where id=?");

        int id=0;
        for(Object key:model.keySet())
        {
            Object value=model.get(key);
            if(value==null)
            {
                continue;
            }
            if(key.toString().equals("id"))
            {
                id=Integer.parseInt(value.toString());
                list.add(id);
                break;
            }
        }
        PreparedStatement preparedStatement = createStatement(sb.toString(),list, connection);
        return preparedStatement.executeUpdate();
    }
    private String getPrefixedLinedString(String str)
    {
        return this.config.prefix()+str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }
    private <E> E CheckExecute(Object tempate,ICheck<E> checker) throws Exception
    {
        String tableName = getTableName(tempate);

        ConcurrentHashMap model = new ConcurrentHashMap();
        ConcurrentHashMap modelType = new ConcurrentHashMap();
        Field[] fields=tempate.getClass().getDeclaredFields();
        //Reporter.Current.info("fields:"+Arrays.toString(fields));
        ArrayList<String> unNormalColumns=new ArrayList<>();
        for(Field field: fields)
        {
            if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
            {
                continue;
            }
            int length=0;
            if(field.get(tempate)!=null) {
                Object value=field.get(tempate);
                if(field.getType().isEnum())
                {
                    String tmpValue=JSON.toJSONString(value);
                    tmpValue=tmpValue.substring(1,tmpValue.length()-1);
                    value=tmpValue;
                    length=((String) value).length();
                }
                else if(!(value instanceof Integer)
                        &&  !(value instanceof String)
                        &&  !(value instanceof Long)
                        &&  !(value instanceof Boolean)
                )
                {
                    value=JSON.toJSONString(value);
                    length=((String) value).length();
                    //Reporter.Current.info(tableName+":"+field.getName()+":"+value);
                }
                model.put(field.getName(), value);
                if(value instanceof Long)
                {
                    length=101;
                }
                else if(value instanceof String)
                {
                    length=((String) value).length();
                }
            }
            if(length>100)
            {
                unNormalColumns.add(field.getName());
            }
            modelType.put(field.getName(),field.getType().getSimpleName());
        }
        //String normalColumn=String.join(",",unNormalColumns);
        //Reporter.Current.info("~~~normalColumn:"+tableName+":"+normalColumn);

        Connection connection=null;
        try
        {
            connection = dataSource.getConnection();
            if (!config.autoTableStruct())
            {
                E result=null;
                result = checker.afterCheck(connection, tableName, model, modelType);
                return result;
            }
            String unNormalColumn=String.join(",",unNormalColumns);
            if(hasCheckedCache.containsKey(tableName))
            {
                List<String> a=Arrays.asList(hasCheckedCache.get(tableName).split(","));
                unNormalColumns.removeAll(a);
                if(unNormalColumns.size()==0)
                {
                    E result=null;
                    result = checker.afterCheck(connection, tableName, model, modelType);
                    return result;
                }
                unNormalColumns.addAll(a);
                unNormalColumn=String.join(",",unNormalColumns);
            }
            int errorCode=0;
            ResultSet rs =null;
            PreparedStatement preparedStatement = createStatement("desc `" + tableName + "`",null, connection);
            try
            {
                rs = preparedStatement.executeQuery();
            }
            catch (SQLSyntaxErrorException e)
            {
                errorCode=e.getErrorCode();
            }
            if(errorCode==1146)
            {
                createTableForModel(connection,tableName,model,modelType);
                hasCheckedCache.put(tableName, unNormalColumn);
                return checker.afterCheck(connection,tableName,model,modelType);
            }

            ConcurrentHashMap alterAdd = new ConcurrentHashMap();
            ConcurrentHashMap alterUpdate = new ConcurrentHashMap();
            ConcurrentHashMap<String,Integer> existsKeys = new ConcurrentHashMap<String,Integer>();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                String field=rs.getString("Field");
                String type=rs.getString("Type");
                existsKeys.put(field,1);
                if(model.containsKey(field))
                {
                    Object fieldvalue=model.get(field);
                    if(type.startsWith("varchar")&&fieldvalue!=null&&fieldvalue.toString().length()>100)
                    {
                        alterUpdate.put(field,fieldvalue);
                    }
                }
            }

            for(Object key:modelType.keySet())
            {
                if(!existsKeys.containsKey(key.toString()))
                {
                    if(!model.containsKey(key))
                    {
                        switch (modelType.get(key).toString())
                        {
                            case "Long":
                                alterAdd.put(key.toString(),new Long(0L));
                                break;
                            case "Integer":
                                alterAdd.put(key.toString(),new Integer(0));
                                break;
                            case "String":
                                alterAdd.put(key.toString(),"");
                                break;
                            case "Boolean":
                                alterAdd.put(key.toString(),Boolean.FALSE);
                                break;
                            default:
                                alterAdd.put(key.toString(),"");
                        }
                    }
                    else
                    {
                        alterAdd.put(key.toString(), model.get(key));
                    }
                }
            }

            if(alterAdd.size()>0||alterUpdate.size()>0)
            {
                alterModel(connection,tableName,alterAdd,alterUpdate,modelType);
            }
            hasCheckedCache.put(tableName,unNormalColumn);
            return checker.afterCheck(connection,tableName,model,modelType);
        }finally {
            if(connection!=null)
            {
                try {
                    connection.close();
                }
                catch (Exception e){}
            }
        }
    }


    public String getTableName(Object tempate)
    {
        String tableName= getPrefixedLinedString(tempate.getClass().getSimpleName());
        return tableName;
    }

    private void createTableForModel(Connection connection,String tableName, ConcurrentHashMap model,ConcurrentHashMap modelType) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (");
        sb.append("`id` int(11) NOT NULL AUTO_INCREMENT,");

        for(Object key:modelType.keySet())
        {
            if(key.equals("id"))
            {
                continue;
            }
            Object value=null;
            if(model.containsKey(key))
            {
                value=model.get(key);
            }

            String type=modelType.get(key).toString();
            if(type.equals("Integer") )
            {
                sb.append("`"+key+"` int(11) NOT NULL DEFAULT '0' ,");
            }
            else if(type.equals("Long") )
            {
                sb.append("`"+key+"` bigint(20) NOT NULL DEFAULT '0' ,");
            }
            else if(type.equals("Boolean"))
            {
                sb.append("`"+key+"` tinyint(4) NOT NULL DEFAULT '0' ,");
            }
            else
            {
                if(value==null)
                {
                    sb.append("`"+key+"` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                }
                else
                {
                    String valueString =value.toString();

                    if (valueString.length() <= 100) {
                        sb.append("`" + key + "` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                    } else {
                        sb.append("`" + key + "` text COLLATE utf8mb4_unicode_ci,");
                    }
                }
            }
        }

        sb.append("`create_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
        sb.append("`update_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',");
        sb.append("PRIMARY KEY (`id`)");
        sb.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        PreparedStatement preparedStatement = createStatement(sb.toString(),null, connection);
        preparedStatement.executeUpdate();
    }

    private void alterModel(Connection connection,String tableName, ConcurrentHashMap alterAdd,ConcurrentHashMap alterUpdate,ConcurrentHashMap modelType) throws SQLException {

        StringBuilder sb = new StringBuilder();
        sb.append("alter table `").append(tableName).append("` ");
        if(alterAdd.size()>0) {
            for (Object key : modelType.keySet()) {
                Object value = null;
                if (!alterAdd.containsKey(key.toString())) {
                    continue;
                }

                value = alterAdd.get(key.toString());

                String type = modelType.get(key).toString();
                if (type.equals("Integer") ) {
                    sb.append("add column `" + key + "` int(11) NOT NULL DEFAULT '0' ,");
                }
                else if (type.equals("Long") ) {
                    sb.append("add column `" + key + "` bigint(20) NOT NULL DEFAULT '0' ,");
                }
                else if (type.equals("Boolean")) {
                    sb.append("add column `" + key + "` tinyint(4) NOT NULL DEFAULT '0' ,");
                } else {
                    if (value == null) {
                        sb.append("add column `" + key + "` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                    } else {
                        String valueString =value.toString();
                        if (valueString.length() <= 100) {
                            sb.append("add column `" + key + "` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                        } else {
                            sb.append("add column `" + key + "` text COLLATE utf8mb4_unicode_ci,");
                        }
                    }
                }
            }
        }
        if(alterUpdate.size()>0) {
            for (Object key : modelType.keySet()) {
                Object value = null;
                if(key.toString().equals("id"))
                {
                    continue;
                }
                if (!alterUpdate.containsKey(key.toString())) {
                    continue;
                }
                value = alterUpdate.get(key);

                String type = modelType.get(key).toString();
                if (type.equals("Integer") ) {
                    sb.append("modify column `" + key + "` int(11) NOT NULL DEFAULT '0' ,");
                }
                else if (type.equals("Long") ) {
                    sb.append("modify column `" + key + "` bigint(20) NOT NULL DEFAULT '0' ,");
                }
                else if (type.equals("Boolean")) {
                    sb.append("modify column `" + key + "` tinyint(4) NOT NULL DEFAULT '0' ,");
                } else {
                    if (value == null) {
                        sb.append("modify column `" + key + "` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                    } else {
                        String valueString ="";
                        if(type.equals("String"))
                        {
                            valueString=value.toString();
                        }
                        else
                        {
                            valueString=JSON.toJSONString(value);
                        }
                        if (valueString.length() <= 100) {
                            sb.append("modify column `" + key + "` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT '',");
                        } else {
                            sb.append("modify column `" + key + "` text COLLATE utf8mb4_unicode_ci,");
                        }
                    }
                }
            }
        }
        sb.deleteCharAt(sb.length()-1);
        PreparedStatement preparedStatement = createStatement(sb.toString(),null, connection);
        preparedStatement.executeUpdate();
    }
    public < E > DataResult<E> query(String sql, List<String> params, Class<?> T) throws Exception {
        Connection connection=null;
        try {
            connection=dataSource.getConnection();
            PreparedStatement preparedStatement = createStatement(sql, params, connection);
            ResultSet rs = preparedStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int colum = metaData.getColumnCount();
            ArrayList<String> columnNames = new ArrayList<String>();
            for (int i = 1; i <= colum; i++) {
                String columName = metaData.getColumnLabel(i);
                columnNames.add(columName);
            }
            //Reporter.log("sql:" + sql);
            //Reporter.log("parameter:" + String.join(",", params));
            //Reporter.log("columnNames:" + String.join(",", columnNames));
            Constructor constructor = T.getConstructor();
            Field[] fields = T.getDeclaredFields();
            //Reporter.log("fields count" + fields.length);
            ArrayList<Field> fieldList = new ArrayList<Field>();
            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }
                if (columnNames.contains(field.getName())) {
                    fieldList.add(field);
                }
            }
            DataResult<E> list = new DataResult<E>();
            while (rs.next()) {
                E obj = (E)constructor.newInstance();
                for (Field field : fieldList) {
                    String value = rs.getString(field.getName());
                    if (setFieldValue(obj, field, value))
                    {
                        continue;
                    }
                }
                list.add(obj);
            }
            //Reporter.log("list count:" + list.size());
            return list;
        }finally {
            if(connection!=null)
            {
                try {
                    connection.close();
                }
                catch (Exception e){}
            }
        }
    }

    private <E> boolean setFieldValue(E obj, Field field, String value) throws Exception
    {
        if(value==null)
        {
            return true;
        }
        if (field.getType().equals(Integer.class)) {
            field.set(obj, Integer.valueOf(value));
        }
        else if (field.getType().equals(Long.class)) {
            field.set(obj, Long.valueOf(value));
        }
        else if (field.getType().equals(Boolean.class)) {
            if(value.equals("1"))
            {
                field.set(obj, Boolean.TRUE);
            }
            else
            {
                field.set(obj, Boolean.FALSE);
            }
        } else if (field.getType().equals(Date.class)) {
            if (value != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                field.set(obj, sdf.parse(value));
            }

        }
        else if (field.getType().equals(Byte.class))
        {
            field.set(obj, Byte.valueOf(value));
        }
        else if (field.getType().equals(String.class))
        {
            field.set(obj, value);
        }
        else if (field.getType().isEnum())
        {
            if(value.startsWith("\"")&&value.endsWith("\""))
            {
                value=value.substring(1,value.length()-1);
            }
            Enum a =Enum.valueOf((Class)(field.getType()),value);
            field.set(obj, a);
        }
        else if (field.getType().equals(ArrayList.class))
        {
            Type subType=((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

            Class<?> clazz = TypeToken.of(subType).getRawType();
            ArrayList newList = new ArrayList();
            JSON.parseArray(value).forEach(item->{
                if(item==null)
                {
                    return;
                }
                if(item instanceof Integer
                        ||item instanceof String
                        ||item instanceof Long
                        ||item instanceof Boolean
                )
                {
                    newList.add(item);
                    return;
                }
                try
                {
                    Object in=clazz.newInstance();
                    for(Field field1:clazz.getDeclaredFields())
                    {
                        if(Modifier.isStatic(field1.getModifiers())||Modifier.isFinal(field1.getModifiers()))
                        {
                            continue;
                        }
                        setFieldValue(in,field1,JSON.toJSONString(item));
                    }
                    newList.add(in);
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
                newList.add(item);
            });
            field.set(obj, newList);
        }
        else {
            try
            {
                Class<?> classz = TypeToken.of(field.getType()).getRawType();

                JSONObject out = JSON.parseObject(value);
                E in = (E) classz.newInstance();
                for (Field field1 : classz.getDeclaredFields())
                {
                    if(Modifier.isStatic(field1.getModifiers())||Modifier.isFinal(field1.getModifiers()))
                    {
                        continue;
                    }
                    if (out.containsKey(field1.getName()) && out.get(field1) != null)
                    {
                        setFieldValue(in, field1, JSON.toJSONString(out.get(field1)));
                    }
                }
                field.set(obj, in);
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        }
        return false;
    }

    private  Long insert( String sql, List<? extends Object> params) throws  SQLException {
        Connection connection=null;
        try {
            connection=dataSource.getConnection();

            PreparedStatement preparedStatement = createStatement(sql, params, connection);
            int executeRowNum = preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next())
            {
                Long id=rs.getLong(1);

                return id;
            }
            else{
                if(sql.toLowerCase().indexOf("ignore")<0)
                {
                    throw  new SQLException("没有生成的主键");
                }
                return 0L;
            }
        }finally {
            if(connection!=null)
            {
                try {
                    connection.close();
                }
                catch (Exception e){}
            }
        }
    }

    public  long execute(String sql, List<? extends Object> params) throws  SQLException {
        Connection connection=null;
        try {
            connection=dataSource.getConnection();
            PreparedStatement preparedStatement = createStatement(sql, params, connection);
            long executeRowNum = preparedStatement.executeUpdate();
            return executeRowNum;
        }finally {
            if(connection!=null)
            {
                try {
                    connection.close();
                }
                catch (Exception e){}
            }
        }
    }
    private  PreparedStatement createStatement(String sql, List<? extends Object> params,Connection connection) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        if(params!=null) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
        }
        String rawSql=preparedStatement.toString();
        int index=rawSql.indexOf("ClientPreparedStatement");
        index=rawSql.indexOf(":",index+1);
        if(this.config.debugSql())
        {
            iApp.debug("-seq-SQL---->" + rawSql.substring(index + 1, rawSql.length()));
        }
        return preparedStatement;
    }
}
