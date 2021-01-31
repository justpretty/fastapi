package com.souher.sdk.business;

import com.souher.sdk.extend.iForceTry;
import com.souher.sdk.extend.iWechatMiniprogram;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.interfaces.iOnEveryMinute;
import com.souher.sdk.model.WechatMiniprogram;

public class WechatMiniprogramServer implements iOnEveryMinute
{
    @Override
    public void onEveryMinute(Long tick) throws Exception
    {
        long currentTimeMillis=System.currentTimeMillis();
        WechatMiniprogram model=WechatMiniprogram.first(WechatMiniprogram.class);
        if(!model.hasId())
        {
            model.appid=iAppConfig.wechatConfig().appid();
            model.secret=iAppConfig.wechatConfig().appsecret();
        }

        if(model.expire_tick==null||currentTimeMillis-model.expire_tick>6000000)
        {
            model.token= iForceTry.run(iWechatMiniprogram::getToken);
            model.expire_tick=currentTimeMillis+7200;
        }
        model.save();

    }
}
