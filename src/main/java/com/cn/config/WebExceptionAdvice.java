package com.cn.config;

import com.cn.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    // 统一捕获整个项目中所有 Controller 抛出的运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("服务器异常: {}", e.getMessage(), e);
        return Result.fail("服务器异常: " + e.getMessage());
    }
}
