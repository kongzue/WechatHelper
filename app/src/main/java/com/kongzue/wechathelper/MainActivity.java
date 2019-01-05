package com.kongzue.wechathelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kongzue.wechatsdkhelper.WeChatPayUtil;
import com.kongzue.wechatsdkhelper.interfaces.OnWXPayListener;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
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
