安卓签名证书使用说明
=======================

生成时间: 2026-05-22 06:27:08
密钥算法: RSA
密钥大小: 4096
有效期: 10 年

密钥别名: com.zhouyi.studio
密钥库密码: Yao990815,

== 使用方法 ==

1. Android Studio 配置:
   菜单 File > Project Structure > Signing
   添加签名配置，选择生成的 .p12 或 .keystore 文件
   输入别名和密码

2. Gradle 配置 (build.gradle):
   android {
       signingConfigs {
           release {
               storeFile file('android.keystore')
               storePassword 'Yao990815,'
               keyAlias 'com.zhouyi.studio'
               keyPassword 'Yao990815,'
           }
       }
   }

3. 命令行签名:
   jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1
     -keystore com.zhouyi.studio.keystore -storepass Yao990815,
     app.apk com.zhouyi.studio

注意: 请妥善保管密钥库文件和密码！
      一旦丢失，已发布的应用将无法更新！
