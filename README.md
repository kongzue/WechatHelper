# Kongzue WechatHelper
Kongzue WechatHelper 是微信 SDK 辅助组件，提供登录、支付和分享三个模块。

<a href="https://github.com/kongzue/StackLabel/">
<img src="https://img.shields.io/badge/StackLabel-1.0.0.4-green.svg" alt="Kongzue StackLabel">
</a>
<a href="https://bintray.com/myzchh/maven/StackLabel/1.0.0.4/link">
<img src="https://img.shields.io/badge/Maven-1.0.0.4-blue.svg" alt="Maven">
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
- 本组件支付模块需要使用网络请求框架与微信接口进行数据交互，因此需要引入 BaseOkHttpV3 组件库，请保证使用 3.0.5 以上版本：
```
implementation 'com.kongzue.baseokhttp_v3:baseokhttp_v3:3.0.8'
```

### 引入

1) 从 Maven 仓库或 jCenter 引入：
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.wechathelper</groupId>
  <artifactId>wechatsdkhelper</artifactId>
  <version>1.1.0</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.wechathelper:wechatsdkhelper:1.1.0'
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
});
```

### 使用微信支付

要使用支付功能首先需要商户号（MCH_ID），服务端的回调接口地址（NOTIFY_URL），已经具备二者可以通过以下代码初始化微信支付：
```
WeChatPayUtil.initPay(MCH_ID,NOTIFY_URL);
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
v1.1.0:
- 正式发布；

