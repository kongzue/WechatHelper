package com.kongzue.wechatsdkhelper.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.kongzue.wechatsdkhelper.WeChatPayUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import static com.kongzue.wechatsdkhelper.WeChatHelper.*;

/**
 * Created by myzcx on 2018/3/5.
 */

public class BaseWXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    
    private Context me;
    
    private IWXAPI api;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.pay_result);
        me = this;
        
        api = WXAPIFactory.createWXAPI(this, WeChatPayUtil.getAppId());
        api.handleIntent(getIntent(), this);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }
    
    @Override
    public void onReq(BaseReq req) {
    }
    
    private boolean isReturnCode = false;
    private int errorCode;
    
    @Override
    public void onResp(final BaseResp resp) {
        isReturnCode = true;
        this.errorCode = resp.errCode;
        if (DEBUGMODE) {
            log("onPayFinish, errCode = " + errorCode + "  getType = " + resp.getType());
        }
    
        if (resp.errCode==-1){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(me);
            builder.setTitle("提示");
            builder.setCancelable(true);
            builder.setMessage("微信支付异常，请稍候重试");
            builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    WeChatPayUtil.getOnWXPayListener().onError(errorCode);
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            builder.show();
            return;
        }
        
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            //doSomething
            doFinish(errorCode);
        }
    }
    
    private void doFinish(int code) {
        if (DEBUGMODE) log("doFinish.code: " + code);
        switch (code) {
            case 0:
                if (WeChatPayUtil.getOnWXPayListener() != null) {
                    if (DEBUGMODE)
                        Log.i(">>>", "OnWXPayListener: onSuccess");
                    WeChatPayUtil.getOnWXPayListener().onSuccess(
                            WeChatPayUtil.getOrderNo()
                    );
                }
                finish();
                break;
            case -1:
                //可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                loge("支付失败");
                if (DEBUGMODE) Log.i(">>>", "OnWXPayListener: onError");
                if (WeChatPayUtil.getOnWXPayListener() != null) {
                    WeChatPayUtil.getOnWXPayListener().onError(errorCode);
                }
                finish();
                break;
            case -2:
                //用户取消
                if (DEBUGMODE) Log.i(">>>", "OnWXPayListener: onCancel");
                if (WeChatPayUtil.getOnWXPayListener() != null) {
                    WeChatPayUtil.getOnWXPayListener().onCancel();
                }
                finish();
                break;
            default:
                finish();
                break;
        }
    }
    
    private int times = 0;
    
    private void toast(final Object obj) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(me, obj.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void toastLong(final Object obj) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(me, obj.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
    
    }
    
    private void log(String s) {
        if (DEBUGMODE) Log.i(">>>", s);
    }
    
    private void loge(String s) {
        if (DEBUGMODE) Log.e(">>>", s);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(">>>", "run: isReturnCode=" + isReturnCode);
                if (!isReturnCode) {
                    finish();
                }
            }
        }, 1000);
    }
}