package com.kongzue.wechatsdkhelper.interfaces;

public interface OnWXPayListener {
    
    void onSuccess(String orderNo);
    
    void onCancel();
    
    void onError(int errorStatus);

}
