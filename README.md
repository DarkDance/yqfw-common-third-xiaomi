### 接入包说明
本接入包提供了小米米家接口的接入，并提供了微信消息、支付回调接口的处理方法。

* 引入依赖包
```xml
<dependency>
    <groupId>cn.jzyunqi</groupId>
    <artifactId>yqfw-common-third-xiaomi</artifactId>
    <version>${yqfw.version}</version>
</dependency>
```
* 引入配置
```java
@Import({XiaomiConfig.class})
```
* 配置自己的小米账号
```java
@Bean
public XiaomiAuthRepository xiaomiAuthRepository() {
    return () -> List.of(
            new XiaomiAuth("account1", "password1"),
            new XiaomiAuth("account2", "password2")
    );
}
```
