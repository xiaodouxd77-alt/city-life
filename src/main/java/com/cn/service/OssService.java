package com.cn.service;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.cn.config.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class OssService {

    @Resource
    private OSS ossClient;

    @Resource
    private OssProperties ossProperties;

    /**
     * 上传文件到 OSS
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String objectName = generateObjectName(originalFilename);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            ossClient.putObject(ossProperties.getBucket(), objectName, file.getInputStream(), metadata);

            String url = ossProperties.getDomain() + "/" + objectName;
            log.debug("文件上传到 OSS 成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("OSS 上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 从 OSS 删除文件
     * @param fileUrl 完整的文件 URL
     */
    public void delete(String fileUrl) {
        try {
            // 从 URL 中提取 objectName
            // URL 格式: https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/a/b/uuid.jpg
            String prefix = ossProperties.getDomain() + "/";
            if (fileUrl != null && fileUrl.startsWith(prefix)) {
                String objectName = fileUrl.substring(prefix.length());
                ossClient.deleteObject(ossProperties.getBucket(), objectName);
                log.debug("OSS 文件删除成功: {}", objectName);
            } else {
                log.warn("无法解析的 OSS URL: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("OSS 删除失败: {}", fileUrl, e);
        }
    }

    private String generateObjectName(String originalFilename) {
        String suffix = "jpg";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            suffix = StrUtil.subAfter(originalFilename, ".", true);
        }
        String uuid = UUID.randomUUID().toString();
        int hash = uuid.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        return StrUtil.format("blogs/{}/{}/{}.{}", d1, d2, uuid, suffix);
    }
}
