package com.cn.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(prefix = "alipay", name = "enabled", havingValue = "true")
public class AlipayConfig {

    @Bean
    public AlipayClient alipayClient(AlipayProperties properties) {
        if (!StringUtils.hasText(properties.getAppId())
                || !StringUtils.hasText(properties.getMerchantPrivateKey())
                || !StringUtils.hasText(properties.getAlipayPublicKey())) {
            throw new IllegalStateException("Alipay is enabled but credentials are incomplete");
        }
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType()
        );
    }
}
