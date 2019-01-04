package com.kongzue.wechatsdkhelper.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.kongzue.baseokhttp.HttpRequest;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.wechatsdkhelper.WeChatLoginUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.kongzue.wechatsdkhelper.WeChatHelper.*;
import static com.kongzue.wechatsdkhelper.WeChatLoginUtil.ERROR_LOGIN;
import static com.kongzue.wechatsdkhelper.WeChatLoginUtil.ERROR_LOGIN_GET_USERINFO;


public class BaseWXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    
    private IWXAPI api;
    
    @Override
    @Deprecated
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        api.registerApp(APP_SECRET);
        try {
            boolean result = api.handleIntent(getIntent(), this);
            if (!result) {
                log("参数不合法，未被SDK处理，退出");
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean isHaveResp = false;
    
    @Override
    public void onReq(BaseReq baseReq) {
        log("onReq:" + baseReq);
    }
    
    @Override
    public void onResp(BaseResp resp) {
        isHaveResp = true;
        log("onResp:" + resp.transaction);
        log("errCode:" + resp.errCode);
        switch (resp.errCode) {
            
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (2 == resp.getType()) {
                    //toast("分享失败");
                    finish();
                } else {
                    //toast("登录错误");
                    finish();
                }
                break;
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case 1:
                        //拿到了微信返回的code,立马再去请求access_token
                        String code = ((SendAuth.Resp) resp).code;
                        log("code=" + code);
                        doLoginByWechat(code);
                        break;
                    case 2:
                        //toast("分享成功");
                        finish();
                        break;
                }
                break;
            
            case BaseResp.ErrCode.ERR_BAN:
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setCancelable(true);
                builder.setMessage("微信登录异常，请稍候重试");
                builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
                builder.show();
                break;
        }
    }
    
    private void doLoginByWechat(String code) {
        HttpRequest.build(this, "https://api.weixin.qq.com/sns/oauth2/access_token")
                .addParameter("appid", APP_ID)
                .addParameter("secret", APP_SECRET)
                .addParameter("code", code)
                .addParameter("grant_type", "authorization_code")
                .setResponseListener(new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        if (error == null) {
                            response = urlEncodeUTF8(response);
                            log(response.toString());
                            if (error == null) {
                                try {
                                    JSONObject main = new JSONObject(response);
                                    String access_token = main.getString("access_token");
                                    String openid = main.getString("openid");
                                    doGetUserInfo(access_token, openid);
                                } catch (Exception e) {
                                    if (DEBUGMODE) e.printStackTrace();
                                    WeChatLoginUtil.getOnWXLoginListener().onError(ERROR_LOGIN);
                                    finish();
                                }
                            }
                        } else {
                            if (DEBUGMODE) error.printStackTrace();
                            WeChatLoginUtil.getOnWXLoginListener().onError(ERROR_LOGIN);
                            finish();
                        }
                    }
                })
                .skipSSLCheck()
                .doPost();
    }
    
    private void doGetUserInfo(String access_token, String openid) {
        HttpRequest.build(this, "https://api.weixin.qq.com/sns/userinfo")
                .addParameter("access_token", access_token)
                .addParameter("openid", openid)
                .addParameter("lang", "zh_CN")
                .setResponseListener(new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        if (error == null) {
                            try {
                                JSONObject main = new JSONObject(response);
                                String openid = main.getString("openid");
                                String nickname = main.getString("nickname");
                                String sex = main.getString("sex");
                                String language = main.getString("language");
                                String headimgurl = main.getString("headimgurl");
                                String unionid = main.getString("unionid");
                                String province = main.getString("province");
                                String city = main.getString("city");
                                
                                Map<String, String> map = new HashMap<>();
                                map.put("openid", openid);
                                map.put("nickname", nickname);
                                map.put("sex", sex);
                                map.put("language", language);
                                map.put("headimgurl", headimgurl);
                                map.put("unionid", unionid);
                                map.put("province", province);
                                map.put("city", city);
    
                                WeChatLoginUtil.getOnWXLoginListener().onSuccess(map);
                            } catch (Exception e) {
                                if (DEBUGMODE) e.printStackTrace();
                                WeChatLoginUtil.getOnWXLoginListener().onError(ERROR_LOGIN_GET_USERINFO);
                                finish();
                            }
                        }
                    }
                })
                .skipSSLCheck()
                .doPost();
    }
    
    public String urlEncodeUTF8(String str) {
        String result = str;
        try {
            result = new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        api.handleIntent(data, this);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
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
}
