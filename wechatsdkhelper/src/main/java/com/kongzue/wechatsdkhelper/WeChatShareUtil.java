package com.kongzue.wechatsdkhelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;

import static com.kongzue.wechatsdkhelper.WeChatHelper.*;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/5 21:15
 */
public class WeChatShareUtil {
    
    public static final int THUMB_SIZE = 150;
    
    //分享链接到微信用户
    public static void shareLinkToUser(Context me, Link link) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID);
        api.registerApp(APP_ID);
        
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = link.getUrl();
        
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = link.getTitle();
        msg.description = link.getUrl();
        Bitmap thumb = link.getImage();
        msg.thumbData = bmpToByteArray(thumb, true);
        
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }
    
    //分享链接到微信朋友圈
    public static void shareLinkToCircle(Context me, Link link) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID);
        api.registerApp(APP_ID);
        
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = link.getUrl();
        
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = link.getTitle();
        msg.description = link.getUrl();
        Bitmap thumb = link.getImage();
        msg.thumbData = bmpToByteArray(thumb, true);
        
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }
    
    //分享文字到微信用户
    public static void shareTextToUser(Context me, String text) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID);
        api.registerApp(APP_ID);
        
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("textshare");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }
    
    //分享文字到微信朋友圈
    public static void shareTextToCircle(Context me, String text) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID);
        api.registerApp(APP_ID);
        
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("textshare");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }
    
    //分享图片给微信用户
    public static void sharePictureToUser(Context me, Bitmap bitmap) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID, true);
        api.registerApp(APP_ID);
        
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        
        //生成缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        
        //执行分享
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }
    
    //分享图片到朋友圈
    public static void sharePictureToCircle(Context me, Bitmap bitmap) {
        IWXAPI api = WXAPIFactory.createWXAPI(me, APP_ID, true);
        api.registerApp(APP_ID);
        
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        
        //生成缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
        
        //执行分享
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }
    
    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
    
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
        
        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        
        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                                 localByteArrayOutputStream
            );
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                if (DEBUGMODE) e.printStackTrace();
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }
    
    public class Link {
        
        private String title;
        private String url;
        private Bitmap image;
        
        public Link(String title, String url, Bitmap image) {
            this.title = title;
            this.url = url;
            this.image = image;
        }
        
        public Link(Context context, String title, String url, int imageResId) {
            this.title = title;
            this.url = url;
            this.image = BitmapFactory.decodeResource(context.getResources(), imageResId);
        }
        
        public String getUrl() {
            return url;
        }
        
        public Link setUrl(String url) {
            this.url = url;
            return this;
        }
        
        public String getTitle() {
            return title;
        }
        
        public Link setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Bitmap getImage() {
            return image;
        }
        
        public void setImage(Bitmap image) {
            this.image = image;
        }
    }
}
