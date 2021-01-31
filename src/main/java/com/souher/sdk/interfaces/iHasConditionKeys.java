package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataModelRelation;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.database.DatabaseOptions;
import com.souher.sdk.extend.Reflector;

public interface iHasConditionKeys
{

    String[] conditionKeys();

    default DataResult<DataModelRelation> handleConditionKeys() throws Exception
    {
        DataResult<DataModelRelation> dataResult=new DataResult();
        DataResult.castArray(this.conditionKeys()).forEachForcely(a->{
            int index=a.indexOf(":");

            if(index<0)
            {
                throw new Exception("配置条件key错误:"+this.getClass().getSimpleName()+":"+a);
            }
            String[] b=a.split(":");
            if(b.length!=3)
            {
                throw new Exception("配置条件key错误1:"+this.getClass().getSimpleName()+":"+a);
            }

            Class<? extends DataModel> cls=Reflector.Default.searchDataModelClass(b[1]);
            DataModel dataModel=cls.newInstance();
            dataModel.option().append(DatabaseOptions.raw,"#TABLE#."+b[2]);
//            iApp.debug(a,"a");
//            iApp.debug(b,"b");
//            iApp.debug(cls.getSimpleName(),"cls");
//            iApp.debug(this.getClass().getSimpleName(),"this.getclass");
            DataResult<DataModelRelation> result=((DataModel)this).WithParents(cls.newInstance(),b[0],b[2]);
            dataResult.addAll(result);

        });

        return dataResult;
    }
}
