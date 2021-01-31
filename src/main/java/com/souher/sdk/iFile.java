package com.souher.sdk;


import com.souher.sdk.extend.SystemFunction;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface iFile
{
    static ArrayList<String> readLines(String fileName) throws Exception
    {
        ArrayList<String> lines=new ArrayList<>();
        FileReader reader = new FileReader(fileName);

        BufferedReader br = new BufferedReader(reader);
        br.lines().forEach(a->{
            lines.add(a);
        });
        br.close();
        reader.close();
        return lines;
    }
    static boolean exists(String filename)
    {
        return new File(filename).exists();
    }

    static String readString(String fileName) throws Exception{
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            iApp.error(e);
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            iApp.error(e);
            return null;
        }
    }
    static void copyFile(String sourcePath,String destPath) throws Exception
    {
        File dest=new File(destPath);

        if(dest.getName().indexOf(".")<0||destPath.endsWith("/"))
        {
            File src=new File(sourcePath);
            dest.mkdirs();
            if(!destPath.endsWith("/"))
            {
                destPath+="/";
            }
            destPath+=src.getName();
            dest=new File(destPath);
        }
        else
        {
            File floder = dest.getParentFile();
            if (!floder.exists())
            {
                floder.mkdirs();
            }
        }
        iFile.write(dest,iFile.readString(sourcePath));
    }
    static void forEachFile(String path, SystemFunction<File,Boolean> handler) throws Exception
    {
        File file = new File(path);
        LinkedList<File> list = new LinkedList<>();
        if (file.exists()) {
            if (null == file.listFiles()) {

                return;
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
                        if(!handler.accept(f))
                        {
                            return;
                        }
                    }
                }
            }
        } else {
            iApp.error("文件不存在!");
        }
    }
    static String getImageBase64(String imagePath) throws Exception
    {
        String extension=iFile.getExtension(imagePath);
        byte[] fileByte = null;
        try {
            File file = new File(imagePath);
            fileByte = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "data:image/"+extension+";base64," + Base64.encodeBase64String(fileByte);
    }
    static String getExtension(String filename){
        int a=filename.lastIndexOf(".");
        if(a<0)
        {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")+1);
    }
    static String getMineType(String filename){
        try {
            String mimeType =new Future<String>()
            {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning)
                {
                    return false;
                }

                @Override
                public boolean isCancelled()
                {
                    return false;
                }

                @Override
                public boolean isDone()
                {
                    return false;
                }
                 boolean isBMP(byte[] buf){
                    byte[] markBuf = "BM".getBytes();  //BMP图片文件的前两个字节
                    return compare(buf, markBuf);
                }

                 boolean isICON(byte[] buf) {
                    byte[] markBuf = {0, 0, 1, 0, 1, 0, 32, 32};
                    return compare(buf, markBuf);
                }
                 boolean isWEBP(byte[] buf) {
                    byte[] markBuf = "RIFF".getBytes(); //WebP图片识别符
                    return compare(buf, markBuf);
                }

                 boolean isGIF(byte[] buf) {

                    byte[] markBuf = "GIF89a".getBytes(); //GIF识别符
                    if(compare(buf, markBuf))
                    {
                        return true;
                    }
                    markBuf = "GIF87a".getBytes(); //GIF识别符
                    if(compare(buf, markBuf))
                    {
                        return true;
                    }
                    return false;
                }


                 boolean isPNG(byte[] buf) {

                    byte[] markBuf = {(byte) 0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A}; //PNG识别符
                    // new String(buf).indexOf("PNG")>0 //也可以使用这种方式
                    return compare(buf, markBuf);
                }

                 boolean isJPEGHeader(byte[] buf) {
                    byte[] markBuf = {(byte) 0xff, (byte) 0xd8}; //JPEG开始符

                    return compare(buf, markBuf);
                }

                 boolean isJPEGFooter(byte[] buf)//JPEG结束符
                {
                    byte[] markBuf = {(byte) 0xff, (byte) 0xd9};
                    return compare(buf, markBuf);
                }
                 boolean compare(byte[] buf, byte[] markBuf) {
                    for (int i = 0; i < markBuf.length; i++) {
                        byte b = markBuf[i];
                        byte a = buf[i];

                        if(a!=b){
                            return false;
                        }
                    }
                    return true;
                }

                 byte[] readInputStreamAt(FileInputStream fis, long skiplength, int length) throws IOException
                {
                    byte[] buf = new byte[length];
                    fis.skip(skiplength);  //
                    int read = fis.read(buf,0,length);
                    return buf;
                }
                @Override
                public String get()
                {
                    FileInputStream fis = null;
                    try {
                        File f = new File(filename);
                        if(!f.exists() || f.isDirectory() || f.length()<8) {
                            throw new IOException("the file ["+f.getAbsolutePath()+"] is not image !");
                        }

                        fis= new FileInputStream(f);
                        byte[] bufHeaders = readInputStreamAt(fis,0,8);
                        if(isJPEGHeader(bufHeaders))
                        {
                            long skiplength = f.length()-2-8; //第一次读取时已经读了8个byte,因此需要减掉
                            byte[] bufFooters = readInputStreamAt(fis, skiplength, 2);
                            if(isJPEGFooter(bufFooters))
                            {
                                return "jpeg";
                            }
                        }
                        if(isPNG(bufHeaders))
                        {
                            return "png";
                        }
                        if(isGIF(bufHeaders)){

                            return "gif";
                        }
                        if(isWEBP(bufHeaders))
                        {
                            return "webp";
                        }
                        if(isBMP(bufHeaders))
                        {
                            return "bmp";
                        }
                        if(isICON(bufHeaders))
                        {
                            return "ico";
                        }
                        throw new IOException("the image's format is unkown!");

                    } catch (Exception e) {
                        iApp.error(e);
                    }finally{
                        try {
                            if(fis!=null) fis.close();
                        } catch (Exception e) {
                        }
                    }
                    return null;
                }

                @Override
                public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
                {
                    return null;
                }
            }.get();

            String result= "image/"+mimeType;
            iApp.debug(result);
            return result;
        } catch (Exception e) {
            iApp.error(e);
        }
        return null;
    }

    static void write(File file,String content) throws Exception
    {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(content);
        out.close();
    }

    static void writeLines(File file,ArrayList<String> content) throws Exception
    {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        content.forEach(a->{
            try
            {
                out.write(a);
                out.newLine();
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        });
        out.close();
    }
}
