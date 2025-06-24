package com.cqaz.adstatistics;

public enum AdEventTwo {
    AD_SHOW("app_ad_success_show"),//成功展示广告
    AD_COMPLETE("app_ad_watch_complete"),//观看完整视频
    AD_CLICK("app_ad_click");//点击广告

    private final String constant;

    AdEventTwo(String event) {
        this.constant = event;
    }

    public String getConstant() {
        return constant;
    }
}
