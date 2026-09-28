package com.cn.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

@Slf4j
@Configuration
public class OssConfig {

    @Resource
    private OssProperties ossProperties;

    @Bean
    public OSS ossClient() {
        OSS client = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        log.info("OSS 客户端初始化成功，bucket: {}, endpoint: {}",
                ossProperties.getBucket(), ossProperties.getEndpoint());
        return client;
    }
}
