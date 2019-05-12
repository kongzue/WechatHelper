package com.kongzue.wechatsdkhelper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.kongzue.baseokhttp.HttpRequest;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.wechatsdkhelper.interfaces.OnWXPayListener;
import com.kongzue.wechatsdkhelper.util.MD5;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import static com.kongzue.wechatsdkhelper.WeChatHelper.*;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Created by myzcx on 2018/3/5.
 */

public class WeChatPayUtil {
    private static OnWXPayListener onWXPayListener;                                             //回调
    
    private static String MCH_ID = "";                                                          //微信支付分配的商户号
    private static String NOTIFY_URL;                                                           //接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
    private static String STORE_KEY = "";                                                       //商户平台设置的密钥key
    
    private String orderNo;                                                                     //商户系统内部订单号
    private String price;                                                                       //订单价格
    private String productName;                                                                 //商品名
    
    private IWXAPI api;
    private Activity context;
    
    private static WeChatPayUtil weChatPayUtil;
    
    private WeChatPayUtil() {
    }
    
    public static void initPay(String MCH_ID, String NOTIFY_URL) {
        WeChatPayUtil.MCH_ID = MCH_ID;
        WeChatPayUtil.NOTIFY_URL = NOTIFY_URL;
    }
    
    public static void doPay(Activity context, String price, String orderNo, String productName, OnWXPayListener listener) {
        synchronized (WeChatPayUtil.class) {
            if (!isInstallWechat(context)){
                listener.onError(ERROR_NOT_INSTALL_WECHAT);
                return;
            }
            if (weChatPayUtil == null) {
                weChatPayUtil = new WeChatPayUtil();
            }
            weChatPayUtil.orderNo = orderNo;
            weChatPayUtil.price = price;
            weChatPayUtil.productName = productName;
            weChatPayUtil.context = context;
            weChatPayUtil.api = WXAPIFactory.createWXAPI(context, APP_ID);
            weChatPayUtil.api.registerApp(APP_ID);
            onWXPayListener = listener;
            weChatPayUtil.takeOrder();
        }
    }
    
    private void takeOrder() {
        if (orderNo == null || orderNo.isEmpty()) {
            loge("订单号orderNo不能为空");
            return;
        }
        log("统一下单开始————————————————————————————");
        // 统一下单
        HttpRequest.build(context, "https://api.mch.weixin.qq.com/pay/unifiedorder")
                .setStringParameter(xmlInfo(genProductArgs()))
                .setResponseListener(new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        if (error == null) {
                            log("统一下单结束————————————————————————————");
                            //如果顺利的话,这里就可以获取到微信返回给我们的信息了,当然如果不顺利的话,检查一下前几步有没有错误吧....
                            log(response);
                            
                            XmlPullParser parser = Xml.newPullParser();
                            StringReader stringReader = new StringReader(response);
                            try {
                                parser.setInput(stringReader);
                                
                                int eventType = parser.getEventType();
                                
                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    String nodeName = parser.getName();
                                    
                                    switch (eventType) {
                                        case XmlPullParser.START_TAG:
                                            if ("prepay_id".equals(nodeName))
                                                prepay_id = parser.nextText();
                                            else if ("nonce_str".equals(nodeName)) {
                                                nonce_str = parser.nextText();
                                            } else if ("sign".equals(nodeName)) {
                                                sign = parser.nextText();
                                            }
                                            break;
                                    }
                                    //这一行代码不能丢,我把这丢了,然后,死循环了...
                                    eventType = parser.next();
                                }
                                
                            } catch (Exception e) {
                                e.printStackTrace();
                                toast("获取订单数据异常");
                            }
                            
                            stringReader.close();
                            
                            startPay();
                        } else {
                            toast("获取订单错误");
                        }
                    }
                })
                .skipSSLCheck()
                .doPost();
    }
    
    //用来接收服务器返回的prepay_id参数
    String prepay_id = "";
    String nonce_str = "";
    //我到现在都不明白服务器返回给我这个值有毛用
    String sign = "";
    
    private void startPay() {
        if (prepay_id == null || prepay_id.isEmpty()) {
            log("prepay_id为空");
            return;
        }
        if (nonce_str == null || nonce_str.isEmpty()) {
            log("nonce_str为空");
            return;
        }
        log("开始支付流程————————————————————————————");
        try {
            String time = System.currentTimeMillis() / 1000 + "";
            
            final SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
            parameters.put("appid", APP_ID);
            parameters.put("partnerid", MCH_ID);
            parameters.put("prepayid", prepay_id);
            parameters.put("noncestr", nonce_str);
            parameters.put("timestamp", time);
            parameters.put("package", "Sign=WXPay");
            
            PayReq req = new PayReq();
            req.appId = APP_ID;
            req.partnerId = MCH_ID;
            req.prepayId = prepay_id;
            req.nonceStr = nonce_str;
            req.timeStamp = time;
            req.packageValue = "Sign=WXPay";
            req.sign = createSign(parameters);
            
            //toast("开始支付");
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            api.sendReq(req);
            log("结束支付流程————————————————————————————");
        } catch (Exception e) {
            toast("支付异常");
            e.printStackTrace();
        }
    }
    
    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        String randomStr = sb.toString();
        return randomStr;
    }
    
    public String createSign(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        //这个partnerkey是需要自己进行设置的,要登陆你的微信的商户帐号(注意是商户,不是开放平台帐号),然后到api什么接口安全之类的那去设置,然后获取到
        sb.append("key=" + STORE_KEY);
        log(sb.toString());
        String sign = MD5.getMD5(sb.toString()).toUpperCase();
        log("sign的值为" + sign);
        return sign;
    }
    
    private Unifiedorder genProductArgs() {
        Unifiedorder unifiedorder = new Unifiedorder();
        unifiedorder.setAppid(APP_ID);
        unifiedorder.setMch_id(MCH_ID);
        //上面提到的获取随机数的方法
        final String nonce_str = getRandomStringByLength(30);
        unifiedorder.setNonce_str(nonce_str);
        unifiedorder.setBody(productName);
        //order_id就是订单号
        unifiedorder.setOut_trade_no(orderNo);
        //总金额
        unifiedorder.setTotal_fee(price);
        //ip地址
        unifiedorder.setSpbill_create_ip("127.0.0.1");
        //支付成功的回调地址
        unifiedorder.setNotify_url(NOTIFY_URL);
        unifiedorder.setTrade_type("APP");
        
        final SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", APP_ID);
        parameters.put("mch_id", MCH_ID);
        parameters.put("nonce_str", nonce_str);
        parameters.put("body", productName);
        parameters.put("out_trade_no", orderNo);
        parameters.put("total_fee", price);
        parameters.put("spbill_create_ip", "127.0.0.1");
        parameters.put("notify_url", NOTIFY_URL);
        parameters.put("trade_type", "APP");
        
        unifiedorder.setSign(createSign(parameters));
        return unifiedorder;
    }
    
    public String xmlInfo(Unifiedorder unifiedorder) {
        if (unifiedorder != null) {
            StringBuffer bf = new StringBuffer();
            bf.append("<xml>");
            
            bf.append("<appid><![CDATA[");
            bf.append(unifiedorder.getAppid());
            bf.append("]]></appid>");
            
            
            bf.append("<body><![CDATA[");
            bf.append(unifiedorder.getBody());
            bf.append("]]></body>");
            
            
            bf.append("<mch_id><![CDATA[");
            bf.append(unifiedorder.getMch_id());
            bf.append("]]></mch_id>");
            
            bf.append("<nonce_str><![CDATA[");
            bf.append(unifiedorder.getNonce_str());
            bf.append("]]></nonce_str>");
            
            bf.append("<notify_url><![CDATA[");
            bf.append(unifiedorder.getNotify_url());
            bf.append("]]></notify_url>");
            
            bf.append("<out_trade_no><![CDATA[");
            bf.append(unifiedorder.getOut_trade_no());
            bf.append("]]></out_trade_no>");
            
            bf.append("<spbill_create_ip><![CDATA[");
            bf.append(unifiedorder.getSpbill_create_ip());
            bf.append("]]></spbill_create_ip>");
            
            bf.append("<total_fee><![CDATA[");
            bf.append(unifiedorder.getTotal_fee());
            bf.append("]]></total_fee>");
            
            bf.append("<trade_type><![CDATA[");
            bf.append(unifiedorder.getTrade_type());
            bf.append("]]></trade_type>");
            
            
            bf.append("<sign><![CDATA[");
            bf.append(unifiedorder.getSign());
            bf.append("]]></sign>");
            
            
            bf.append("</xml>");
            
            log(bf.toString());
            
            return bf.toString();
        }
        
        return "";
    }
    
    private void log(String s) {
        if (DEBUGMODE) Log.i(">>>", s);
    }
    
    private void loge(String s) {
        if (DEBUGMODE) Log.e(">>>", s);
    }
    
    private void toast(final Object obj) {
        Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
    }
    
    class Unifiedorder {
        private String appid;
        private String mch_id;
        private String nonce_str;
        private String sign;
        private String body;
        private String out_trade_no;
        private String total_fee;
        private String spbill_create_ip;
        private String time_start;
        private String notify_url;
        private String trade_type;
        
        public String getAppid() {
            return appid;
        }
        
        public void setAppid(String appid) {
            this.appid = appid;
        }
        
        public String getMch_id() {
            return mch_id;
        }
        
        public void setMch_id(String mch_id) {
            this.mch_id = mch_id;
        }
        
        public String getNonce_str() {
            return nonce_str;
        }
        
        public void setNonce_str(String nonce_str) {
            this.nonce_str = nonce_str;
        }
        
        public String getSign() {
            return sign;
        }
        
        public void setSign(String sign) {
            this.sign = sign;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public String getOut_trade_no() {
            return out_trade_no;
        }
        
        public void setOut_trade_no(String out_trade_no) {
            this.out_trade_no = out_trade_no;
        }
        
        public String getTotal_fee() {
            return total_fee;
        }
        
        public void setTotal_fee(String total_fee) {
            this.total_fee = total_fee;
        }
        
        public String getSpbill_create_ip() {
            return spbill_create_ip;
        }
        
        public void setSpbill_create_ip(String spbill_create_ip) {
            this.spbill_create_ip = spbill_create_ip;
        }
        
        public String getTime_start() {
            return time_start;
        }
        
        public void setTime_start(String time_start) {
            this.time_start = time_start;
        }
        
        public String getNotify_url() {
            return notify_url;
        }
        
        public void setNotify_url(String notify_url) {
            this.notify_url = notify_url;
        }
        
        public String getTrade_type() {
            return trade_type;
        }
        
        public void setTrade_type(String trade_type) {
            this.trade_type = trade_type;
        }
    }
    
    public static OnWXPayListener getOnWXPayListener() {
        return onWXPayListener;
    }
    
    public static boolean isInstallWechat(Context c) {
        try {
            c.getPackageManager().getApplicationInfo("com.tencent.mm", PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    public static String getAppId() {
        return APP_ID;
    }
    
    public static String getAppSecret() {
        return APP_SECRET;
    }
    
    public static String getMchId() {
        return MCH_ID;
    }
    
    public static String getNotifyUrl() {
        return NOTIFY_URL;
    }
    
    public static String getStoreKey() {
        return STORE_KEY;
    }
    
    public static String getOrderNo() {
        return weChatPayUtil.orderNo;
    }
    
    public static void setStoreKey(String storeKey) {
        STORE_KEY = storeKey;
    }
    
    public static void setMchId(String mchId) {
        MCH_ID = mchId;
    }
    
    public static void setNotifyUrl(String notifyUrl) {
        NOTIFY_URL = notifyUrl;
    }
}
