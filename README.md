# Kongzue WechatHelper
Kongzue WechatHelper 是微信 SDK 辅助组件，提供登录、支付和分享三个模块。

<a href="https://github.com/kongzue/WechatHelper/">
<img src="https://img.shields.io/badge/WechatHelper-1.1.5-green.svg" alt="Kongzue WechatHelper">
</a>
<a href="https://bintray.com/myzchh/maven/WechatHelper/1.1.5/link">
<img src="https://img.shields.io/badge/Maven-1.1.5-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>


### 提示
- 本组件是微信 SDK 辅助组件，因此需要额外引入微信 SDK 库：
```
implementation 'com.tencent.mm.opensdk:wechat-sdk-android-with-mta:+'
```
- 本组件支付模块需要使用网络请求框架与微信接口进行数据交互，因此需要引入 BaseOkHttpV3 组件库，请保证使用 3.1.4 以上版本：
```
implementation 'com.kongzue.baseokhttp_v3:baseokhttp_v3:3.1.5'
```

### 引入

1) 从 Maven 仓库或 jCenter 引入：
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.wechathelper</groupId>
  <artifactId>wechatsdkhelper</artifactId>
  <version>1.1.5</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.wechathelper:wechatsdkhelper:1.1.5'
```

## 开始使用

### 准备过程
要使用微信 SDK 功能，请先前往微信开放平台申请 APP_ID 和 APP_SECRET，并对本组件进行初始化：
```
WeChatHelper.APP_ID = "这里填写你的APP ID"；
WeChatHelper.APP_SECRET = "这里填写你的APP SECRET"；

//需要开启日志打印：
WeChatHelper.DEBUGMODE = true;
```

接下来需要根据微信 SDK 的要求，在你的App包根目录下创建子包“wxapi”，并创建两个用于回调的类，它们分别用于登录和分享回调以及支付回调：

![creat class](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/wechathelper_creat_class.png)

删除其中的代码，并将它们分别继承 BaseWXEntryActivity 和 BaseWXPayEntryActivity：

![edit code](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/wechathelper_edit_code.png)

为了更好的体验，进入 AndroidManifest.xml 将它们的主题设置为透明（本库提供 @style/ActivityEmpty），此时微信回调后就不会显示回调界面了：

![add theme](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/wechathelper_add_theme.png)

可前往<a href="https://github.com/kongzue/WechatHelper/blob/master/app/src/main/AndroidManifest.xml" target="_blank">直接复制</a>

另外需要在 AndroidManifest.xml 中添加以下代码以便微信 SDK 通知注册：
```
<receiver android:name="com.kongzue.wechatsdkhelper.util.WXAppRegister">
    <intent-filter>
        <action android:name="com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP" />
    </intent-filter>
</receiver>
```

### 使用微信登录

使用以下代码创建登录：
```
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
    
    @Override
    public boolean returnCode(String code) {
        return false;                                       //如果 return true，则自行处理登录code，不再由WeChatHelper负责接下来的获取用户信息的事务
    }
});
```

### 使用微信支付

#### 使用纯客户端完成所有支付逻辑

此逻辑环境下，由客户端完成所有支付过程，服务端可通过后续微信服务通过接口回调数据判断支付信息是否合法。

要使用支付功能首先需要商户号（MCH_ID），服务端的回调接口地址（NOTIFY_URL），已经具备二者可以通过以下代码初始化微信支付：
```
WeChatPayUtil.initPay(MCH_ID,NOTIFY_URL);
```

设置商户平台的密钥key（在微信后台配置）
```
WeChatPayUtil.setStoreKey(STORE_KEY);
```

要开始支付流程，首先需要已知一个商品的名称（productName）、价格（price）、内部订单号（orderNo），然后使用以下代码创建支付流程：
```
WeChatPayUtil.doPay(this, price, orderNo, productName, new OnWXPayListener() {
    @Override
    public void onSuccess(String orderNo) {
        //支付成功，返回内部订单号做校验，校验流程需要发送给服务端检查订单是否已被支付
    }
    @Override
    public void onCancel() {
        //支付被取消
    }
    @Override
    public void onError(int errorStatus) {
        //支付错误，错误码可能为：
        //WeChatHelper.ERROR_NOT_INSTALL_WECHAT = -1;       //未安装微信
        //请根据微信返回错误码排查错误原因：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
    }
});
```

#### 使用半服务端半客户端完成支付逻辑

此逻辑环境下，由服务端完成前半段支付参数信息申请后交给客户端完成调起支付流程。

客户端需要以下参数可启动支付流程：价格（price）、订单号（orderNo）、商品名（productName）、凭证（prepayId、nonceStr）、时间戳（timeStamp）、签名（sign）

使用以下接口发起支付流程：
```
WeChatPayUtil.doPay(this, price, orderNo, productName, prepayId, nonceStr, timeStamp, sign, new OnWXPayListener() {
    @Override
    public void onSuccess(String orderNo) {
        //支付成功，返回内部订单号做校验，校验流程需要发送给服务端检查订单是否已被支付
    }
    @Override
    public void onCancel() {
        //支付被取消
    }
    @Override
    public void onError(int errorStatus) {
        //支付错误，错误码可能为：
        //WeChatHelper.ERROR_NOT_INSTALL_WECHAT = -1;       //未安装微信
        //请根据微信返回错误码排查错误原因：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
    }
});
```

#### 支付常见问题
**Q: 支付发起前的准备？**

A: 检查微信后台是否配置正确（商户是否申请、后台是否配置了正确的APK密钥、安卓APP支付权限是否开启、`WeChatHelper.APP_ID` 以及 `WeChatHelper.APP_SECRET` 是否配置等，详见上述文档）

**Q: 支付不成功/异常/闪退/或者其他奇奇怪怪的问题**

A: 开启日志打印 `WeChatHelper.DEBUGMODE = true;`，在控制台中你可以看到绝大多数问题的错误日志；

**Q: 支付提示：微信支付签名验证失败**

A: 检查微信后台配置的 APK 签名是 debug 版的还是 release 版的，配置对 APK 签名才可以支付。

**Q: 支付金额不对**

A: 微信支付金额单位为“分”，即支付1元需要传参数 price 为“100”，且该值请勿传入小数，否则会直接支付失败。

**Q: 在微信后台修改了支付相关设置，但支付还是错误**

A: 请重启手机，微信自己有缓存，在未完整重启前部分流程依然会直接使用老数据。

待续...


### 使用微信分享
要是用微信分享，请参阅以下代码：

#### 分享文字
```
//分享至微信用户：
WeChatShareUtil.shareTextToUser(Context context, String text);
//分享至朋友圈
WeChatShareUtil.shareTextToCircle(Context context, String text);
```

#### 分享链接
```
//创建链接，参数分别为标题、链接和展示图片：
Link link = new Link(title, url, image);
//创建方法2，通过资源id创建：
Link link = new Link(context, title, url, imageResId);

//分享至微信用户：
WeChatShareUtil.shareLinkToUser(Context context, Link link);
//分享至朋友圈
WeChatShareUtil.shareLinkToCircle(Context context, Link link);
```

#### 分享图片
```
//分享至微信用户：
WeChatShareUtil.sharePictureToUser(Context context, Bitmap bitmap);
//分享至朋友圈
WeChatShareUtil.sharePictureToCircle(Context context, Bitmap bitmap);
```

从 1.1.3 版本起，新增图像递减压缩算法以适应微信SDK对分享图片32k字节（实际上是32768）的限制，如果您开启了 WeChatHelper.DEBUGMODE = true 会看到以下日志：

此算法会对您传入的 bitmap 位图进行降低清晰度的不断压缩，直到其压缩到符合微信限制以内。
```
D/>>>: zipBitmap: quality=100   size=509930
D/>>>: zipBitmap: quality=90   size=144679
D/>>>: zipBitmap: quality=80   size=107233
D/>>>: zipBitmap: quality=70   size=91980
D/>>>: zipBitmap: quality=60   size=83841
D/>>>: zipBitmap: quality=50   size=79622
D/>>>: zipBitmap: quality=40   size=72253
D/>>>: zipBitmap: quality=30   size=57459
D/>>>: zipBitmap: quality=20   size=41839
D/>>>: zipBitmap: quality=10   size=25331
```

之所以这么做，因为很多情况下您通过微信 SDK 直接分享图片到用户或朋友圈，会出现没反应的情况，如果此时您查看日志可能遇到 “checkArgs fail, thumbData is invalid” 的微信错误提示，此算法为解决该问题而设计。

## 开源协议
```
Copyright Kongzue WechatHelper

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 更新日志
v1.1.5.1(Beta):
- 更新了支付回调逻辑，在启动支付的 Activity 主线程进行回调；

v1.1.5:
- 升级至最新 BaseOkHttpV3 支持；
- 支持自定义支付逻辑（由服务端负责请求参数后仅使用客户端完成支付流程）；

v1.1.4:
- 修复支付回调的空指针异常 bug；

v1.1.3:
- 新增图像递减压缩算法以适应微信SDK对分享图片32k字节（实际上是32768）的限制；

v1.1.2:
- 增加 receiver；

v1.1.1:
- OnWXLoginListener 新增 returnCode(String code) 回调，方便自行处理 code 的情况。

v1.1.0:
- 正式发布；

