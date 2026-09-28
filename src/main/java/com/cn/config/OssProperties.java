package com.cn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    /** OSS 地域节点 */
    private String endpoint;
    /** Bucket 名称 */
    private String bucket;
    /** AccessKey ID */
    private String accessKeyId;
    /** AccessKey Secret */
    private String accessKeySecret;
    /** OSS 访问域名 */
    private String domain;
}
