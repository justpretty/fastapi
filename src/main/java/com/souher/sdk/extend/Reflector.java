package com.souher.sdk.extend;

import com.souher.sdk.App;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.interfaces.iHasForeignKeys;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Reflector {

    public static Reflector Default = new Reflector();

    private static ArrayList<Class> allClass = new ArrayList<>();
    private static KeyArrayMap<Class,Class> InterfaceClassMap = new KeyArrayMap<Class,Class>();
    private static final String sdkPackageName = "com/souher/sdk/";
    private static ArrayList<Class<? extends DataModel>> DataModelClasses = new ArrayList<Class<? extends DataModel>>();

    public  Class<? extends DataModel> searchDataModelClass(String name)
    {
        AtomicReference<Class<? extends DataModel>> cls=new AtomicReference<>();
        DataModelClasses.forEach(a->{
            if(a.getSimpleName().toLowerCase().equals(name.toLowerCase()))
            {
                cls.set(a);
            }
        });
        return cls.get();
    }

    public DataResult<Class<? extends DataModel>> getClassesByForeignClass(Class<? extends DataModel> cls)
    {
        DataResult<Class<? extends DataModel>> dataResult=new DataResult();

        DataModelClasses.forEach(a->{
            if(!iHasForeignKeys.class.isAssignableFrom(a))
            {
                return;
            }
            try
            {
                ((iHasForeignKeys)a.newInstance()).foreignClasses().forEachForcely(b->{
                    if(b.getSimpleName().equals(cls.getSimpleName().toLowerCase()))
                    {
                        dataResult.add(a);
                    }
                });
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        });
        return dataResult;
    }

    public  ArrayList<Class> getAllClassByInterface(Class clazz) {
        ArrayList<Class> list = new ArrayList<>();
        // 判断是否是一个接口
        if (clazz.isInterface()) {
            try {
                if(allClass.size()==0)
                {
                    String path = iApp.JarFile().getAbsolutePath();
                    String packageName1 = App.mainClass.getPackage().getName();
                    String[] packages1 = packageName1.split("\\.");
                    String packageName="";
                    for(int i=0;i<packages1.length;i++)
                    {
                        packageName+=packages1[i]+"/";
                        if(i>=1)
                        {
                            break;
                        }
                    }

                    allClass=getClassNameByJarPath(path,packageName,true);
                    for(Class t:allClass)
                    {
                        for (Class in : t.getInterfaces()) {
                            InterfaceClassMap.append(in,t);
                        }
                        if(DataModel.class.isAssignableFrom(t))
                        {
                            DataModelClasses.add(t);
                        }
                    }
                }
                list=InterfaceClassMap.get(clazz);
                if(list==null)
                {
                    list=new ArrayList<>();
                }
            } catch (Exception e) {
                iApp.error(e);
                throw new RuntimeException("出现异常"+e.getMessage());
            }
        }
        String[] a=new String[10000];
        StringBuilder sb=new StringBuilder();
        list.forEach(b->{sb.append(b).append(",");});
        return list;
    }


    /**
     * 从一个指定路径下查找所有的类
     *
     * @param packagename
     */
    private  ArrayList<Class> getAllClass(String packagename) {


        List<String> classNameList =  getClassName(packagename);
        ArrayList<Class> list = new ArrayList<>();

        for(String className : classNameList){
            try {
                list.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                iApp.error("load class from name failed:"+className+e.getMessage());
                throw new RuntimeException("load class from name failed:"+className+e.getMessage());
            }
        }

        return list;
    }

    /**
     * 获取某包下所有类
     * @param packageName 包名
     * @return 类的完整名称
     */
    public  List<String> getClassName(String packageName) {

        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();

            if (type.equals("file")) {
                String fileSearchPath = url.getPath();

                fileSearchPath = fileSearchPath.substring(0,fileSearchPath.indexOf("/classes"));

                fileNames = getClassNameByFile(fileSearchPath);
            } else if (type.equals("jar")) {
                try{
                    JarURLConnection jarURLConnection = (JarURLConnection)url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();
                    fileNames = getClassNameByJar(jarFile,packagePath);
                }catch (java.io.IOException e){
                    throw new RuntimeException("open Package URL failed："+e.getMessage());
                }

            }else{
                throw new RuntimeException("file system not support! cannot load MsgProcessor！");
            }
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * @param filePath 文件路径
     * @return 类的完整名称
     */
    private  List<String> getClassNameByFile(String filePath) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                myClassName.addAll(getClassNameByFile(childFile.getPath()));
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }


    private ArrayList<Class> getClassNameByJarPath(String jarFilePath,String packagePath, boolean childPackage) {
        ArrayList<Class> myClassName = new ArrayList<>();
        try {
            if(!jarFilePath.endsWith(".jar"))
            {
                File file = new File(jarFilePath);
                LinkedList<File> list = new LinkedList<>();
                if (file.exists()) {
                    if (null == file.listFiles()) {
                        return myClassName;
                    }
                    list.addAll(Arrays.asList(file.listFiles()));
                    while (!list.isEmpty()) {
                        File[] files = list.removeFirst().listFiles();
                        if (null == files) {
                            continue;
                        }
                        for (File f : files) {
                            if (f.isDirectory()) {
                                list.add(f);
                            } else {
                                String entryName =f.getAbsolutePath();
                                if (entryName.endsWith(".class"))
                                {
                                    if (childPackage)
                                    {

                                        if(entryName.startsWith(jarFilePath+"/"))
                                        {
                                            entryName=entryName.substring(jarFilePath.length()+1);
                                        }
//                                        iApp.print(packagePath+":"+entryName);
                                        if (entryName.startsWith(packagePath)||entryName.startsWith(sdkPackageName))
                                        {
                                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                            if (entryName.length() > 0)
                                            {
                                                Class cls = Class.forName(entryName);
                                                if (cls != null)
                                                {
                                                    myClassName.add(cls);
                                                }
                                            }
                                        }
                                    } else
                                    {
                                        int index = entryName.lastIndexOf("/");
                                        String myPackagePath;
                                        if (index != -1)
                                        {
                                            myPackagePath = entryName.substring(0, index);
                                        } else
                                        {
                                            myPackagePath = entryName;
                                        }
                                        if (myPackagePath.equals(packagePath)||myPackagePath.equals(sdkPackageName))
                                        {
                                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                            if (entryName.length() > 0)
                                            {
                                                Class cls = Class.forName(entryName);
                                                if (cls != null)
                                                {
                                                    myClassName.add(cls);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    iApp.error("文件不存在!");
                }
            }
            else
            {
                JarFile jarFile = new JarFile(jarFilePath);
                Enumeration<JarEntry> entrys = jarFile.entries();
                while (entrys.hasMoreElements())
                {
                    JarEntry jarEntry = entrys.nextElement();
                    String entryName = jarEntry.getName();
                    if (entryName.endsWith(".class"))
                    {
                        if (childPackage)
                        {
                            if (entryName.startsWith(packagePath)||entryName.startsWith(sdkPackageName))
                            {
//                            iApp.print(entryName);
                                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                if (entryName.length() > 0)
                                {
                                    Class cls = Class.forName(entryName);
                                    if (cls != null)
                                    {
                                        myClassName.add(cls);
                                    }
                                }
                            }
                        } else
                        {
                            int index = entryName.lastIndexOf("/");
                            String myPackagePath;
                            if (index != -1)
                            {
                                myPackagePath = entryName.substring(0, index);
                            } else
                            {
                                myPackagePath = entryName;
                            }
                            //Reporter.Default.debug(entryName);
                            if (myPackagePath.equals(packagePath)||myPackagePath.equals(sdkPackageName))
                            {
//                            iApp.print(entryName);
                                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                                if (entryName.length() > 0)
                                {
                                    Class cls = Class.forName(entryName);
                                    if (cls != null)
                                    {
                                        myClassName.add(cls);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            iApp.error(e);
        }
        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * @return 类的完整名称
     */
    private  List<String> getClassNameByJar(JarFile jarFile ,String packagePath) {
        List<String> myClassName = new ArrayList<String>();
        try {
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                //LOG.info("entrys jarfile:"+entryName);
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                    //LOG.debug("Find Class :"+entryName);
                }
            }
        } catch (Exception e) {
            iApp.error("发生异常:"+e.getMessage());
            throw new RuntimeException("发生异常:"+e.getMessage());
        }
        return myClassName;
    }

}
