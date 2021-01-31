package com.souher.sdk;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.DateUtil;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public interface iAliyun
{
    static void uploadFile( File file,String targetPath) throws Exception{
        OSS ossClient = new OSSClientBuilder().build(iAppConfig.aliyunOSSConfig().endpoint(),
                iAppConfig.aliyunOSSConfig().appid(),
                iAppConfig.aliyunOSSConfig().appsecret());

        ossClient.putObject(iAppConfig.aliyunOSSConfig().bucketname(), targetPath, new FileInputStream(file));
        ossClient.shutdown();
    }

    static ArrayList<String> listFiles()
    {
        OSS ossClient = new OSSClientBuilder().build(iAppConfig.aliyunOSSConfig().endpoint(),
                iAppConfig.aliyunOSSConfig().appid(),
                iAppConfig.aliyunOSSConfig().appsecret());
        ArrayList<String> list=new ArrayList<>();
        ObjectListing objectListing = ossClient.listObjects(
                new ListObjectsRequest(iAppConfig.aliyunOSSConfig().bucketname()).withMaxKeys(1000));
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            list.add(objectSummary.getKey());
        }
        ossClient.shutdown();
        return list;
    }
}
