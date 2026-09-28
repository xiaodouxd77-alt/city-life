package com.cn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    private boolean enabled;
    private String appId;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String notifyUrl;
    private String returnUrl;
    private String frontendReturnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "json";
    private String productSubject = "城市点评优惠券";
}
