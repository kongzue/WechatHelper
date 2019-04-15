package com.kongzue.wechatsdkhelper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kongzue.wechatsdkhelper.WeChatHelper;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/4/15 15:41
 */
public class WXAppRegister extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
        
        // 将该app注册到微信
        msgApi.registerApp(WeChatHelper.APP_ID);
    }
}
