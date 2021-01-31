package com.souher.sdk.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.iApp;
import com.souher.sdk.iFile;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequest {

    private String url;
    private boolean isPost=false;
    private ConcurrentHashMap<String,String> queryParameters=new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,String> postParameters=new ConcurrentHashMap<>();
    private JSONObject jsonObject=null;
    private String response;
    public boolean onlyPrintRequest=false;
    private int timeout=5000;
    private File file;
    private String fileTag="file";

    public HttpRequest setFile(File file)
    {
        this.file = file;
        return this;
    }

    public HttpRequest setFileTag(String fileTag)
    {
        this.fileTag = fileTag;
        return this;
    }

    public HttpRequest(boolean isPost) {
        this.isPost=isPost;
    }

    public static HttpRequest NewGetRequest()
    {
        return new HttpRequest(false);
    }

    public static HttpRequest NewPostRequest()
    {
        return new HttpRequest(true);
    }

    public HttpRequest setJsonParameter(JSONObject jsonObject)
    {
        this.jsonObject=jsonObject;
        return this;
    }

    public HttpRequest setUrl(String url)
    {
        this.url=url;
        return this;
    }

    public HttpRequest addQueryParameter(String key,String value)
    {
        this.queryParameters.put(key,value);
        return this;
    }
    public HttpRequest addPostParameter(String key,String value)
    {
        this.postParameters.put(key,value);
        return this;
    }
    public HttpRequest send(int timeout) throws Exception
    {
        this.timeout=timeout;
        return send();
    }


    public HttpRequest send() throws Exception {
        String url = this.url;
        if (queryParameters.size() > 0) {
            if(url.contains("?"))
            {
                url += "&" + getQueryString(queryParameters);
            }
            else
            {
                url += "?" + getQueryString(queryParameters);
            }
        }
        iApp.debug("timeout:"+timeout);
        if (!isPost) {
            if(onlyPrintRequest)
            {
                iApp.debug("request:\n\n------------------------------------------\n   "
                        +url+""
                        +"\n------------------------------------------\n");
                iApp.debug("request:\n\n<<<<<<<<<<   "+url+"\n\n");
            }
            else
            {
                iApp.debug("request:\n\n------------------------------------------\n   "
                        + url + ""
                        + "\n------------------------------------------\n");
                iApp.debug("request:\n\n<<<<<<<<<<   " + url + "\n\n");
                response = get(url);
            }
            if(response.length()<100)
            {
                iApp.debug("response:\n\n" + response + "\n");
            }

            return this;
        }

        if (jsonObject == null)
        {
            String post=getQueryString(postParameters);
            if(onlyPrintRequest)
            {
                iApp.debug("request:\n\n------------------------------------------\n   "
                        +url+"\n   "
                        +post+"\n------------------------------------------\n");
            }
            else
            {
                iApp.debug("request:\n\n------------------------------------------\n   "
                        +url+"\n   "
                        +post+"\n------------------------------------------\n");
                if(file!=null)
                {
                    response = postFile(url, postParameters,fileTag,file);
                }
                else
                {
                    response = post(url, post);
                }
            }

            return this;
        }
        String post=jsonObject.toJSONString();
        if(onlyPrintRequest)
        {
            iApp.debug("request:\n\n------------------------------------------\n   "
                    + url + "\n   "
                    + post + "\n------------------------------------------\n");
        }
        else
        {
            iApp.debug("request:\n\n------------------------------------------\n   "
                    + url + "\n   "
                    + post + "\n------------------------------------------\n");
            response = postJson(url, post);
        }

        return this;
    }

    public String getStringResponse()
    {
        return response;
    }

    public JSONObject getJsonResponse()
    {
        return JSON.parseObject(response);
    }

    public <E> E getObjectResponse(Class T)
    {
        return (E)JSON.parseObject(response,T);
    }

    public <E,F> boolean getObjectResponse(E model,F errorModel) throws Exception {
        return getObjectResponse(model,errorModel,null);
    }
    public <E,F> boolean getObjectResponse(E model,F errorModel,String key,String response1) throws Exception {
        JSONObject obj1=JSON.parseObject(response1);
        JSONObject obj=obj1;
        if(key!=null)
        {
            obj=obj1.getJSONObject(key);
        }
        Field[] fields=model.getClass().getDeclaredFields();
        boolean hasResult=false;
        for(Field field: fields)
        {
            if(obj.containsKey(field.getName()))
            {
                hasResult=true;
                if(field.getType().equals(Integer.class))
                {
                    field.set(model,obj.getIntValue(field.getName()));
                }
                else if(field.getType().equals(Long.class))
                {
                    field.set(model,obj.getLongValue(field.getName()));
                }
                else if(field.getType().equals(Boolean.class))
                {
                    field.set(model,obj.getBooleanValue(field.getName()));
                }
                else if(field.getType().equals(ArrayList.class))
                {
                    Type subType=((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                    Class<?> clazz = com.google.common.reflect.TypeToken.of(subType).getRawType();
                    ArrayList list = new ArrayList();
                    obj.getJSONArray(field.getName()).forEach(item->{
                        try
                        {
                            Object a=clazz.newInstance();
                            F error= (F) errorModel.getClass().newInstance();
                            getObjectResponse(a,error,null,JSON.toJSONString(item));
                            list.add(clazz.cast(a));
                        }
                        catch (Exception e)
                        {
                            iApp.error(e);
                        }
                    });
                    field.set(model,list);
                }
                else if(field.getType().equals(String.class))
                {
                    field.set(model,obj.getString(field.getName()));
                }
                else if(field.getType().equals(JSONObject.class))
                {
                    field.set(model,obj.getJSONObject(field.getName()));
                }
                else
                {
                    Class<?> clazz = com.google.common.reflect.TypeToken.of(field.getGenericType()).getRawType();
                    if(obj.get(field.getName())!=null)
                    {
                        Object a=clazz.newInstance();
                        F error= (F) errorModel.getClass().newInstance();
                        getObjectResponse(a,error,null,JSON.toJSONString(obj.get(field.getName())));
                        field.set(model,clazz.cast(a));
                    }
                }
            }
        }
        if(hasResult)
        {
            return true;
        }
        fields=errorModel.getClass().getDeclaredFields();
        for(Field field: fields)
        {
            if(obj.containsKey(field.getName()))
            {
                if(field.getType().equals(Integer.class))
                {
                    field.set(errorModel,obj.getIntValue(field.getName()));
                }
                else if(field.getType().equals(Long.class))
                {
                    field.set(errorModel,obj.getLongValue(field.getName()));
                }
                else if(field.getType().equals(Boolean.class))
                {
                    field.set(errorModel,obj.getBooleanValue(field.getName()));
                }
                else
                {
                    field.set(errorModel,obj.getString(field.getName()));
                }
            }
        }
        return false;
    }

    public <E,F> boolean getObjectResponse(E model,F errorModel,String key) throws Exception {
        return getObjectResponse(model,errorModel, key, response);
    }



    private String getQueryString(Map<?, ?> data) throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append ( URLEncoder.encode ( (String) pair.getKey (), "UTF-8" ) + "=" );
            queryString.append ( URLEncoder.encode ( (String) pair.getValue (), "UTF-8" ) + "&" );
        }
        if (queryString.length () > 0) {
            queryString.deleteCharAt ( queryString.length () - 1 );
        }
        return queryString.toString ();
    }

    private String get(String url) throws Exception {
        URL httpUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();

        connect(url, connection);
        StringBuilder content = new StringBuilder();
        if (connection.getResponseCode() == 200) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                content.append(str);
            }
            bufferedReader.close();
            return content.toString();
        }
        iApp.error(connection.getResponseCode()+":"+connection.getResponseMessage());
        return "";
    }

    private void connect(String url, HttpURLConnection connection) throws Exception
    {
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(2*timeout);
        int initTimes=5;
        if(timeout<=500)
        {
            initTimes = 3;
        }
        int trytimes=initTimes;

        boolean hasError=false;
        while(trytimes>=0)
        {
            trytimes--;
            try
            {
                connection.connect();
                break;
            }
            catch (Exception e){
                hasError=true;
                if(trytimes<=0)
                {
                    throw new Exception(e.getMessage()+",url:"+url);
                }
            }
        }
        if(hasError)
        {
            int finalTrytimes = trytimes;
            int finalInitTimes = initTimes;
            new Thread(()->{
                iApp.alarm("trytimes:"+(finalInitTimes - finalTrytimes)+",url:"+url);
            }).start();
        }
    }

    private String post(String url, String params) throws Exception {
        URL httpUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
        connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connect(url, connection);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(params);
        bufferedWriter.flush();
        bufferedWriter.close();
        StringBuilder content = new StringBuilder();
        if (connection.getResponseCode() == 200) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                content.append(str);
            }
            bufferedReader.close();
            return content.toString();
        }
        iApp.error(connection.getResponseCode()+":"+connection.getResponseMessage());
        return "";
    }

    private String postFile(String url, ConcurrentHashMap params,String tag, File file) throws Exception {
        URL httpUrl = new URL(url);
        String Boundary = UUID.randomUUID().toString(); // 文件边界

// 1.开启Http连接
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true); // 允许输出

// 2.Http请求行/头
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+Boundary);
        connect(url, connection);
// 3.Http请求体
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        String fileName=file.getName();
        if(!fileName.contains("."))
        {
            fileName+="."+ iFile.getExtension(file.getAbsolutePath());
        }
        out.writeUTF("--"+Boundary+"\r\n"
                +"Content-Disposition: form-data; name=\""+tag+"\"; filename=\""+fileName+"\"\r\n"
                +"Content-Type: "+iFile.getMineType(file.getAbsolutePath())+"; charset=utf-8"+"\r\n\r\n");
        InputStream in = new FileInputStream(file);
        byte[] b = new byte[1024];
        int l = 0;
        while((l = in.read(b)) != -1) out.write(b,0,l); // 写入文件
        out.writeUTF("\r\n--"+Boundary+"--\r\n");
        out.flush();
        out.close();
        in.close();
        StringBuilder content = new StringBuilder();
        if (connection.getResponseCode() == 200) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                content.append(str);
            }
            bufferedReader.close();
            return content.toString();
        }
        iApp.error(connection.getResponseCode()+":"+connection.getResponseMessage());
        return "";
    }

    private String postJson(String url, String params) throws Exception {
        URL httpUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
        connection.setRequestProperty("content-type", "application/json;charset=utf-8");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connect(url, connection);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(params);
        bufferedWriter.flush();
        bufferedWriter.close();
        StringBuilder content = new StringBuilder();
        if (connection.getResponseCode() == 200) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                content.append(str);
            }
            bufferedReader.close();
            return content.toString();
        }

        iApp.error(connection.getResponseCode()+":"+connection.getResponseMessage());
        return "";
    }
}
