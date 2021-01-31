package com.souher.sdk.io;


import com.souher.sdk.iApp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RealTimeFileContentService {

    public static RealTimeFileContentService RealTimeCpuIdleContentService=new RealTimeFileContentService(new java.io.File("/tmp/cpu"));
    public static RealTimeFileContentService RealTimeMemFreeContentService=new RealTimeFileContentService(new java.io.File("/tmp/mem"));

    private java.io.File file;
    private String content;
    private int capacity = 1000;// 字节

    public RealTimeFileContentService(java.io.File file)
    {
        this.file=file;
    }

    public String getStringContent()
    {
        if(content==null)
        {
            return "";
        }
        else
        {
            return content;
        }
    }
    public double getDoubleContent()
    {
        final String b=getStringContent().trim();
        if(b==null||b.isEmpty())
        {
            return -1;
        }
        else
        {
            try
            {
                Double a = Double.parseDouble(b);
                if (a == null)
                {
                    return -1;
                }
                return a;
            }
           catch (Exception e)
           {
               iApp.warning(e);
               return -1;
           }
        }
    }

    public void start()
    {
        try
        {
            new Thread(() -> {
                while (true) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        FileChannel channel=fileInputStream.getChannel();
                        ByteBuffer buffer = ByteBuffer.allocate(capacity);
                        channel.read(buffer);
                        String content1 = new String(buffer.array());
                        if(content1!=null&&!content1.isEmpty())
                        {
                            content=content1;
                        }
                        try {
                            channel.close();
                        } catch (IOException ee) {

                        }
                        try {
                            fileInputStream.close();
                        } catch (IOException ee) {

                        }
                    }
                    catch (Exception e)
                    {
                        iApp.error(e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        iApp.error(e);
                    }
                }
            }).start();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
        finally {

        }
    }
}
