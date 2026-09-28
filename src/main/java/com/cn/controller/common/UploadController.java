package com.cn.controller.common;

import com.cn.dto.Result;
import com.cn.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    @Resource
    private OssService ossService;

    /**
     * 上传图片到阿里云 OSS
     */
    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            String url = ossService.upload(image);
            log.debug("文件上传成功，{}", url);
            return Result.ok(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败");
        }
    }

    /**
     * 从阿里云 OSS 删除图片
     */
    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        try {
            ossService.delete(filename);
            return Result.ok();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.fail("文件删除失败");
        }
    }
}
