package com.cqaz.adstatistics;

public class PayEventBean {
    private float money;//金额
    private String pay_type;//支付方式
    private String order_id;//订单
    private String event;//自定义下单位置

    public PayEventBean(String event, float money, String pay_type, String order_id) {
        this.event = event;
        this.money = money;
        this.pay_type = pay_type;
        this.order_id = order_id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }
}
