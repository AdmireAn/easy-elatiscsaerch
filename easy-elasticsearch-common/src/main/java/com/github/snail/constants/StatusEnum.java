package com.github.snail.constants;

/**
 * @author snail
 * Created on 2022-11-28
 * 返回值枚举定义
 */
public enum StatusEnum {

    SUCCESS("请求成功", 200),
    DEGRADE("%s操作降级", 210),
    NOT_FOUND("资源 %s 不存在", 404),
    BAD_REQUEST("请求参数有异常，状态码%s", 400),

    // 资源异常 1000 ~ 2000
    NO_AVAILABLE_RESOURCE("无可用节点", 1001),


    // 资源异常 2001 ~ 3000
    JSON_PROCESSING_EXCEPTION("Json序列化异常", 1001);

    private String message;

    private int code;

    StatusEnum(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
