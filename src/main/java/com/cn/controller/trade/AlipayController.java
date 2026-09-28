package com.cn.controller.trade;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.cn.config.AlipayProperties;
import com.cn.dto.Result;
import com.cn.service.VoucherOrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Resource
    private VoucherOrderService voucherOrderService;

    @Resource
    private AlipayProperties properties;

    @Autowired(required = false)
    private AlipayClient alipayClient;

    @PostMapping("/pay/{orderId}")
    public Result pay(@PathVariable Long orderId) {
        if (alipayClient == null || !properties.isEnabled()) {
            return Result.fail("支付宝支付未配置");
        }
        Result orderResult = voucherOrderService.createAlipayOrder(orderId);
        if (!Boolean.TRUE.equals(orderResult.getSuccess())) {
            return orderResult;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) orderResult.getData();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setReturnUrl(properties.getReturnUrl());
        request.setBizContent("{"
                + "\"out_trade_no\":\"" + order.get("outTradeNo") + "\","
                + "\"total_amount\":\"" + order.get("totalAmount") + "\","
                + "\"subject\":\"" + escapeJson(String.valueOf(order.get("subject"))) + "\","
                + "\"product_code\":\"QUICK_WAP_WAY\""
                + "}");
        try {
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
            if (!response.isSuccess() || response.getBody() == null) {
                return Result.fail("支付宝下单失败：" + response.getMsg());
            }
            return Result.ok(Map.of("paymentForm", response.getBody()));
        } catch (AlipayApiException e) {
            return Result.fail("支付宝下单失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/notify", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    public String notify(@RequestParam Map<String, String> params) {
        if (!verifySignature(params)) {
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "failure";
        }
        String orderId = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        if (orderId == null || totalAmount == null) {
            return "failure";
        }
        boolean completed = voucherOrderService.completeAlipayPayment(
                Long.valueOf(orderId), new BigDecimal(totalAmount), params.get("trade_no"));
        return completed ? "success" : "failure";
    }

    @GetMapping("/return")
    public void returnUrl(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        if (!verifySignature(params)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "支付宝返回签名校验失败");
            return;
        }
        String orderId = params.get("out_trade_no");
        String target = properties.getFrontendReturnUrl();
        if (target == null || target.isBlank()) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("支付完成，请返回订单页面查看状态");
            return;
        }
        String separator = target.contains("?") ? "&" : "?";
        response.sendRedirect(target + separator + "orderId=" + urlEncode(orderId == null ? "" : orderId));
    }

    private boolean verifySignature(Map<String, String> params) {
        if (alipayClient == null || params == null || params.isEmpty()) {
            return false;
        }
        try {
            return com.alipay.api.internal.util.AlipaySignature.rsaCheckV2(
                    params,
                    properties.getAlipayPublicKey(),
                    properties.getCharset(),
                    properties.getSignType());
        } catch (AlipayApiException e) {
            return false;
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}


