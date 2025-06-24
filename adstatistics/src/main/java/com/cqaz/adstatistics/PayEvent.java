package com.cqaz.adstatistics;

public enum PayEvent {
    PAY_SUCCESS("event_w_app_payed"),//付费成功
    PAY_ORDER("event_2_app_pay_order");//下单未支付或失败
    private final String constant;

    PayEvent(String event) {
        this.constant = event;
    }

    public String getConstant() {
        return constant;
    }
}
