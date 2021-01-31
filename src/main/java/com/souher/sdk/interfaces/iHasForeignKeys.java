package com.souher.sdk.interfaces;

import com.mchange.v1.db.sql.schemarep.ForeignKeyRep;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataModelRelation;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.KeyArrayMap;
import com.souher.sdk.extend.KeyMapMap;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public interface iHasForeignKeys
{

    String[] foreignKeys();

    default DataResult<Class<? extends DataModel>> foreignClasses() throws Exception
    {
        DataResult<Class<? extends DataModel>> result=new DataResult<>();
        DataResult.castArray(this.foreignKeys()).forEachForcely(a->{
            String b=a;
            int index=a.indexOf(":");
            if(index>0)
            {
                a=a.substring(0,index);
                b=b.substring(index+1);
            }
            else
            {
                if (b.endsWith("_id"))
                {
                    b = b.substring(0, a.length() - 3);
                }
                b=b.replaceAll("_","");
            }

            Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(b);
            if(cls!=null)
            {
                result.add(cls);
            }
        });
        return result;
    }

    default KeyArrayMap<Class<? extends DataModel>,String> foreignClassAndMyColumn() throws Exception
    {
        KeyArrayMap<Class<? extends DataModel>,String> result=new KeyArrayMap<>();
        DataResult.castArray(this.foreignKeys()).forEachForcely(a->{
            String b=a;
            int index=a.indexOf(":");
            if(index>0)
            {
                a=a.substring(0,index);
                b=b.substring(index+1);
            }
            else
            {
                if (b.endsWith("_id"))
                {
                    b = b.substring(0, a.length() - 3);
                }
                b=b.replaceAll("_","");
            }

            Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(b);
            if(cls!=null)
            {
                result.append(cls,a);
            }
        });
        return result;
    }

    default DataResult<DataModelRelation> handleForeignKeys() throws Exception
    {
        DataResult<DataModelRelation> dataResult=new DataResult();
        DataResult.castArray(this.foreignKeys()).forEachForcely(a->{
            String b=a;
            int index=a.indexOf(":");
            if(index>0)
            {
                a=a.substring(0,index);
                b=b.substring(index+1);
            }
            else
            {
                if (b.endsWith("_id"))
                {
                    b = b.substring(0, a.length() - 3);
                }
                b=b.replaceAll("_","");
            }

            Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(b);
//            iApp.debug(a,"a");
//            iApp.debug(b,"b");
//            iApp.debug(cls.getSimpleName(),"cls");
//            iApp.debug(this.getClass().getSimpleName(),"this.getclass");
            DataResult<DataModelRelation> result=((DataModel)this).WithParents(cls.newInstance(),a,"id");
            dataResult.addAll(result);

        });

        return dataResult;
    }
}
