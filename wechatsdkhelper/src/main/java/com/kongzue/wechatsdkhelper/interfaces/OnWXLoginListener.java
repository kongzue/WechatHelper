package com.kongzue.wechatsdkhelper.interfaces;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/4 23:55
 */
public interface OnWXLoginListener {
    
    void onSuccess(Map<String, String> result);
    
    void onCancel();
    
    void onError(int errorStatus);
    
}
