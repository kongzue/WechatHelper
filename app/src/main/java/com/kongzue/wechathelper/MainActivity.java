package com.kongzue.wechathelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kongzue.wechatsdkhelper.WeChatLoginUtil;
import com.kongzue.wechatsdkhelper.WeChatPayUtil;
import com.kongzue.wechatsdkhelper.interfaces.OnWXLoginListener;
import com.kongzue.wechatsdkhelper.interfaces.OnWXPayListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        WeChatLoginUtil.doLogin(this, new OnWXLoginListener() {
            @Override
            public void onSuccess(Map<String, String> result) {
                String openid = result.get("openid");
                String nickname = result.get("nickname");
                String sex = result.get("sex");
                String language = result.get("language");
                String headimgurl = result.get("headimgurl");
                String unionid = result.get("unionid");
                String province = result.get("province");
                String city = result.get("city");
            }
    
            @Override
            public void onCancel() {
                //登录被取消
            }
    
            @Override
            public void onError(int errorStatus) {
                //发生错误，错误码可能为：
                //WeChatLoginUtil.ERROR_LOGIN = -7;                 //登录错误
                //WeChatLoginUtil.ERROR_LOGIN_GET_USERINFO = -8;    //无法获取用户信息
                //WeChatHelper.ERROR_NOT_INSTALL_WECHAT = -1;       //未安装微信
            }
        });
    
        //WeChatPayUtil.initPay(MCH_ID,NOTIFY_URL);
        //WeChatPayUtil.doPay(this, "100", orderNo, "商品名称", new OnWXPayListener() {
        //    @Override
        //    public void onSuccess(String orderNo) {
        //
        //    }
        //
        //    @Override
        //    public void onCancel() {
        //
        //    }
        //
        //    @Override
        //    public void onError(int errorStatus) {
        //
        //    }
        //});
    }
}
