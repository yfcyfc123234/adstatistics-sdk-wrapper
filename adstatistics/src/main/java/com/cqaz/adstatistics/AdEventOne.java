package com.cqaz.adstatistics;

public enum AdEventOne {
    LAUNCH("app_ad_launch_screen"),//开屏
    INNER("app_ad_inner"),//插屏
    INCENTIVE("app_ad_incentive");//激励视频
    private final String constant;

    AdEventOne(String event) {
        this.constant = event;
    }

    public String getConstant() {
        return constant;
    }
}
