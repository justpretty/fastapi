package local;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iAliyun;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.iFile;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater
{
    private  static String from=System.getProperty("user.home");
    private  static String target=System.getProperty("user.home");
    private static int ossMinSize=1000;
    private static String[] ignores=new String[]{

    };
    static ConcurrentHashMap<String,String> map=new ConcurrentHashMap<>();
    public static void main(String[] args) {

        try
        {
            from+="/Desktop/UIv3";
            target+="/Documents/javascript/qianxian";
            handle();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    private static ArrayList<String> list=new ArrayList<>();
    private static void handle() throws Exception
    {
        list=iAliyun.listFiles();
        iFile.forEachFile(from,file->{
            try
            {
                String extension=iFile.getExtension(file.getAbsolutePath());
                if(extension.equals("html"))
                {
                    String pageName=file.getParentFile().getName();
                    if(DataResult.castArray(ignores).contains(pageName))
                    {
                        return true;
                    }
                    String imageFloder=file.getParent()+"/images";
                    String styleFilePath=file.getParent()+"/styles.css";
                    File styleFile=new File(styleFilePath);
                    handleOnePage(file,styleFile,imageFloder,pageName);
                    return true;
                }
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
            return true;
        });
    }

    private static void handleOnePage(File html,File styleFile,String imageFloder,String pageName) throws Exception
    {
        iApp.print(html.getAbsolutePath().substring(from.length()+1));
        judgeStore(html);
        deleteLines(html);
        tagwork(html,pageName);
        scriptwork(html,pageName);
        stylework(html,styleFile,imageFloder,pageName);
        copy( html, pageName);
        pagework(pageName);
        jswork(pageName);
    }
    private static void jswork(String pageName) throws Exception
    {
        String pagePath=target+"/pages/"+pageName+"/"+pageName+".js";
        File file=new File(pagePath);
        if(!file.exists())
        {
            String content="import s from \"../../s.js\";\n" +
                    "export default\n" +
                    "{\n" +
                    "\tmethods: {\n" +
                    "\t\ttap(c){\n" +
                    "\t\t\t\n" +
                    "\t\t},\n" +
                    "\t\tt(c){s.t(c)||this.tap(c);},\n" +
                    "\t}\n" +
                    "}";
            iFile.write(file,content);
        }

    }
    private static void pagework(String pageName) throws Exception
    {
        String pagePath=target+"/pages.json";
        String content=iFile.readString(pagePath);
        JSONObject jsonObject=JSONObject.parseObject(content, Feature.OrderedField);
        AtomicBoolean hasPage= new AtomicBoolean(false);
        String path="pages/"+pageName+"/"+pageName;
        jsonObject.getJSONArray("pages").forEach(a->{
            if(((JSONObject)a).getString("path").equals(path))
            {
                hasPage.set(true);
            }
        });
        if(!hasPage.get())
        {
            JSONObject object=new JSONObject();
            object.put("path",path);
            JSONObject style=new JSONObject();
            style.put("navigationBarTitleText","");
            style.put("navigationBarBackgroundColor","#ffffff");
            style.put("enablePullDownRefresh",true);
            object.put("style",style);
            jsonObject.getJSONArray("pages").add(object);
            content= JSON.toJSONString(jsonObject,true);
            iFile.write(new File(pagePath),content);
        }
    }
    private static void copy(File html,String pageName) throws Exception
    {
        String targetFloderPath=target+"/pages/"+pageName;
        File floder=new File(targetFloderPath);
        if(!floder.exists())
        {
            floder.mkdirs();
        }
        String targetFilePath=targetFloderPath+"/"+pageName+".vue";
        iFile.write(new File(targetFilePath),iFile.readString(html.getAbsolutePath()));
    }
    private static void stylework(File html,File styleFile,String imageFloder,String pageName) throws Exception
    {
        //iApp.print(html.getAbsolutePath().substring(from.length()+1)+":stylework");
        String newLine=System.getProperty("line.separator");
        String content=iFile.readString(html.getAbsolutePath());
        StringBuilder sb=new StringBuilder();
        ArrayList<String> lines=iFile.readLines(styleFile.getAbsolutePath());
        for (int i = 0; i < lines.size(); i++)
        {
            if(i>5)
            {
                String line=lines.get(i);
                if(line.startsWith(".sk-asset "))
                {
                    line=".s "+line.substring(10);
                }
                else if(line.startsWith(".sk-asset."))
                {
                    line=".s."+line.substring(10);
                }
                sb.append(line).append(newLine);
            }
        }
        String result = sb.toString();
        result = handleRpx(result);
        result = handleAdded(result);
        result = handleImage(result,imageFloder,pageName);
        content=handleRpx(content);
        AtomicReference<String> finalContent = new AtomicReference<String>();
        finalContent.set(result);
        map.forEach((a, b)->{
            finalContent.set(finalContent.get().replaceAll(Pattern.quote("."+a+" "),"."+b+" "));
        });
        result=finalContent.get();
        iFile.write(html,content+newLine+"<style>"+newLine+
                "@import './"+pageName+".css';"+newLine+
                "@import './"+pageName+".scss';"+newLine+
                "</style>");

        String targetFloderPath=target+"/pages/"+pageName;
        File floder=new File(targetFloderPath);
        if(!floder.exists())
        {
            floder.mkdirs();
        }
        File cssFile=new File(targetFloderPath+"/"+pageName+".css");
        iFile.write(cssFile,result);
        File cssFile2=new File(targetFloderPath+"/"+pageName+".scss");
        if(!cssFile2.exists())
        {
            iFile.write(cssFile2,"");
        }
    }
    private static String handleAdded(String stylecontent) throws Exception
    {
        String newLine=System.getProperty("line.separator");
        String[] lines=stylecontent.split(newLine);
        StringBuilder sb=new StringBuilder();
        boolean ignoreStart=false;
        for (String line : lines)
        {
            if(line.startsWith("@media only screen and "))
            {
                ignoreStart=true;
                continue;
            }
            if(ignoreStart)
            {
                if(line.startsWith("}"))
                {
                    ignoreStart=false;
                }
                continue;
            }
            sb.append(line).append(newLine);
        }
        return sb.toString();
    }

    private static String handleImage(String stylecontent,String imageFloder,String pageName) throws Exception
    {
        String linePattern=" url\\(\"images/([^\"]*)\"\\)";
        Pattern pattern=Pattern.compile(linePattern,Pattern.CASE_INSENSITIVE);
        String a= stylecontent;
        Matcher matcher= pattern.matcher(a);
        while (matcher.find())
        {
            String imagePath=imageFloder+"/"+matcher.group(1);
            int index=imagePath.lastIndexOf(".");
            imagePath=imagePath.substring(0,index)+"@2x"+imagePath.substring(index);
            File file=new File(imagePath);
            if(file.length()<ossMinSize)
            {
                String base64=iFile.getImageBase64(imagePath);
                String newString=" url(\""+base64+"\")";
                a=a.replaceAll(Pattern.quote(matcher.group()),newString);
            }
            else
            {
                String ossPath="static/"+pageName+"/"+file.getName();
                //String targetPath=target+"/"+ossPath;
                //iFile.copyFile(imagePath,targetPath);
               // if(!list.contains(ossPath))
                {
                    iApp.print("uploading:"+ossPath);
                    iAliyun.uploadFile(file,ossPath);
                }
                String newString=" url(\"https://"+ iAppConfig.aliyunOSSConfig().domain()+"/"+ossPath+"\")";
                a=a.replaceAll(Pattern.quote(matcher.group()),newString);
            }
            matcher= pattern.matcher(a);
        }
        return a;
    }
    private static String handleRpx(String stylecontent)
    {
        String linePattern=" ([0-9.\\-]*)px([^a-zA-Z0-9]|$)";
        StringBuilder sb=new StringBuilder();
        Pattern pattern=Pattern.compile(linePattern,Pattern.CASE_INSENSITIVE);
        String a= stylecontent;
        Matcher matcher= pattern.matcher(a);
        while (matcher.find())
        {
            String px=matcher.group(1);
            double rpx=Double.parseDouble(px)*2;
            String rpxString=String.valueOf(rpx);
            if(Math.floor(rpx)==rpx)
            {
                rpxString=new java.text.DecimalFormat("0").format(rpx);
            }
            String newString=" "+rpxString+"rpx"+matcher.group(2);
            a=a.replace(matcher.group(),newString);
            matcher= pattern.matcher(a);
        }
        sb.append(a);

        return sb.toString();
    }

    private static void scriptwork(File html,String pageName) throws Exception
    {
        //iApp.print(html.getAbsolutePath().substring(from.length()+1)+":scriptwork");
        String newLine=System.getProperty("line.separator");
        String content=iFile.readString(html.getAbsolutePath());
        content+=newLine+
               "<script>\n" +
                "import js from \"./"+pageName+".js\";\n" +
                "export default js;\n" +
                "</script>"
            +newLine;
        iFile.write(html,content);
    }

    private static void tagwork(File html,String pageName) throws Exception
    {
        ArrayList<String> lines=iFile.readLines(html.getAbsolutePath());
        String newLine=System.getProperty("line.separator");
        StringBuilder sb=new StringBuilder();
        Pattern pattern=Pattern.compile("class='sk-asset ([^']*)'");

        for (int i = 0; i < lines.size(); i++)
        {
            String content=lines.get(i);
            Matcher matcher=pattern.matcher(content);
            if(matcher.find())
            {
                String newClass=pageName+"_"+i;
                map.put(matcher.group(1),pageName+"_"+i);
                content=content.replaceAll(matcher.group(),"class='s "+newClass+"' @tap=\"t('"+newClass+"')\" ");
                content=content.replaceAll("<div ","<view ");
            }
            else
            {
                content=content.replaceAll("<div ","<view ");
            }
            content=content.replaceAll("</div>","</view>");
            sb.append(content).append(newLine);
        }

        String content=sb.toString();
        content="<template>"+System.getProperty("line.separator")+content+"</template>";
        iFile.write(html,content);
    }
    private static void deleteLines(File html) throws Exception
    {
        //iApp.print(html.getAbsolutePath().substring(from.length()+1)+":deleteLines");
        ArrayList<String> list=iFile.readLines(html.getAbsolutePath());
        if(list.size()>0&&!list.get(0).startsWith("<html"))
        {
            iApp.print(html.getAbsolutePath()+":ignore");
            return;
        }
        ArrayList<String> newList=new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
        {
            if(i>=6&&i<list.size()-2)
            {
                newList.add(list.get(i));
            }
        }
        iFile.writeLines(html,newList);
    }

    private static void judgeStore(File html) throws Exception
    {
        String fileName=html.getAbsolutePath()+".bak";
        File file=new File(fileName);
        if(file.exists())
        {
            restoreFile( html);
        }
        else
        {
            storeFile(html);
        }
    }
    private static void storeFile(File html) throws Exception
    {
        String fileName=html.getAbsolutePath()+".bak";
        iFile.write(new File(fileName),iFile.readString(html.getAbsolutePath()));
    }
    private static void restoreFile(File html) throws Exception
    {
        String fileName=html.getAbsolutePath()+".bak";
        iFile.write(html,iFile.readString(fileName));
    }
}
