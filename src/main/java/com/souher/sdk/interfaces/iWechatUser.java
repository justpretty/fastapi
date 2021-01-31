package com.souher.sdk.interfaces;

public interface iWechatUser
{
    boolean checkEnabled();

    iWechatUser userLoginByOpenid(String openid) throws Exception;

    iWechatUser userSaveInfo(int id, String nickname, String pic, int gender,String country,String province,String city,String language) throws Exception;
}
