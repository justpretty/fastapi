

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.business.StatisticsDataCreator;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iString;
import com.souher.sdk.interfaces.iApiSelectableModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DevelopTest
{

    public static void main(String[] args) {
        try
        {


            DataResult dataResult=new DataResult();
            dataResult.add("sss");
            Object a=dataResult;
            iApp.debug(JSON.toJSONString(a));
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }
}
