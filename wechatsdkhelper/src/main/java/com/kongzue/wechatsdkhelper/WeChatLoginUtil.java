package com.kongzue.wechatsdkhelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;

import com.kongzue.wechatsdkhelper.interfaces.OnWXLoginListener;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import static com.kongzue.wechatsdkhelper.WeChatHelper.*;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/4 23:35
 */
public class WeChatLoginUtil {
    
    public static final int ERROR_LOGIN = -7;
    public static final int ERROR_LOGIN_GET_USERINFO = -8;
    
    private static OnWXLoginListener onWXLoginListener;
    
    public static void doLogin(Activity me, OnWXLoginListener onWXLoginListener) {
        if (WeChatPayUtil.isInstallWechat(me)) {
            WeChatLoginUtil.onWXLoginListener = onWXLoginListener;
            IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID);
            api.handleIntent(me.getIntent(), new IWXAPIEventHandler() {
                @Override
                public void onReq(BaseReq baseReq) {
                
                }
                
                @Override
                public void onResp(BaseResp baseResp) {
                
                }
            });
            
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = getAndroidId(me);
            api.sendReq(req);
        } else {
            onWXLoginListener.onError(ERROR_NOT_INSTALL_WECHAT);
        }
    }
    
    public static String getAndroidId(Context me) {
        String androidID = Settings.Secure.getString(me.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidID;
    }
    
    public static OnWXLoginListener getOnWXLoginListener() {
        return onWXLoginListener;
    }
}
