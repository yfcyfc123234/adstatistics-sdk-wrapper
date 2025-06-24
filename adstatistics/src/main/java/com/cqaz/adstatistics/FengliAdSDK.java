package com.cqaz.adstatistics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.blankj.utilcode.util.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FengliAdSDK {
    public static final String TAG = FengliAdSDK.class.getSimpleName();
    private static final String HOST = "https://ad.utap.vip/app/event";

    @SuppressLint("StaticFieldLeak")
    private static FengliAdSDK instance;

    private volatile String userID;
    private String onlyID;
    private String oaid;
    private String appKey;
    private String mChannel;
    private String ipv6;

    private volatile boolean isInit;

    private Context context;

    private boolean isChangeInfo, isChangeIP;

    private SharedPreferences mPrefs;
    private ScheduledExecutorService scheduledExecutorService, executor;

    private int againReQuest;//重新请求数量

    private boolean ticketAppBackground = true;

    public static synchronized FengliAdSDK getInstance() {
        if (instance == null) instance = new FengliAdSDK();
        return instance;
    }

    private SharedPreferences getPrefs() {
        if (mPrefs == null)
            mPrefs = context.getSharedPreferences("FengliAdSDK", Context.MODE_PRIVATE);
        return mPrefs;
    }

    public String getOaid() {
        return oaid;
    }

    public void setOaid(String oaid) {
        this.oaid = oaid;
    }

    public void setOnlyID(String onlyID) {
        this.onlyID = onlyID;
    }

    public boolean isTicketAppBackground() {
        return ticketAppBackground;
    }

    public void setTicketAppBackground(boolean ticketAppBackground) {
        this.ticketAppBackground = ticketAppBackground;
    }

    private HttpUtils httpUtils;

    /**
     * 切换账号后要重新输入
     *
     * @param userID userID
     */
    public void setUserID(String userID) {
        if (!TextUtils.equals(this.userID, userID)) {
            isChangeInfo = true;
        }

        this.userID = userID;

        if (isChangeInfo && !TextUtils.isEmpty(userID)) {
            ticket();
        }
    }

    /***
     *
     * @param context context
     * @param key appKey
     * @param channel 渠道
     * @param deviceID 设备唯一标识
     * @param userID 用户id
     */
    public void init(Context context, String key, String channel, String userID, String deviceID) {
        init(context, key, channel, userID, deviceID, null);
    }

    /***
     *
     * @param ctx ctx
     * @param key appKey
     * @param channel 渠道
     * @param userID 用户id
     * @param deviceID 设备唯一标识
     * @param listener 网络结果返回 可以是null
     */
    public void init(Context ctx, String key, String channel, String userID, String deviceID, OnHttpRequestListener listener) {
        this.userID = userID;
        this.mChannel = channel;
        this.context = ctx.getApplicationContext();
        onlyID = deviceID;
        this.appKey = key;
        if (TextUtils.isEmpty(appKey)) {
            FengliLog.e(TAG, "appKey不能为空");
            return;
        }
        if (TextUtils.isEmpty(onlyID)) {
            FengliLog.e(TAG, "deviceID不能为空");
            return;
        }
        httpUtils = new HttpUtils();
        isChangeInfo = true;
        JSONObject object = getCommentJSON();
        try {
            object.put("event_type1", "app_launch");
            object.put("event_type2", "app_launch");
            object.put("event_type3", "");
            object.put("event_data", "");
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        send(object, new OnHttpRequestListener() {
            @Override
            public void onFail(int code, String msg) {
                switch (code) {
                    case 401:
                        if (msg != null) {
                            FengliLog.e(TAG, msg);
                        }
                        break;
                    case 408:
                        if (againReQuest < 3) {
                            init(context, appKey, mChannel, userID, onlyID);
                        } else {
                            againReQuest = 0;
                            executor = Executors.newScheduledThreadPool(1);
                            executor.schedule(() -> init(context, appKey, mChannel, userID, onlyID), 2, TimeUnit.SECONDS);
                        }
                        FengliLog.e(TAG, againReQuest + "");
                        againReQuest++;
                        break;
                }
                if (listener != null) {
                    listener.onFail(code, msg);
                }
            }

            @Override
            public void request(String msg) {
                if (listener != null) {
                    listener.request(msg);
                }
            }

            @Override
            public void onSuccess(String result) {
                try {
                    if (executor != null) {
                        executor.shutdown();
                        executor = null;
                    }
                } catch (Exception e) {
                    FengliLog.e(TAG, e);
                }

                isInit = true;
                time();
                if (listener != null) {
                    listener.onSuccess(result);
                }
            }
        });
    }

    //返回是否调用过初始化
    public boolean isInit() {
        return isInit;
    }

    private void ticket() {
        if (!isInit) {
            FengliLog.e(TAG, "ticket 未初始化");
            return;
        }
        FengliLog.e(TAG, "ticket");
        if (httpUtils == null) {
            httpUtils = new HttpUtils();
        }

        if (!ticketAppBackground && !AppUtils.isAppForeground()) {
            return;
        }

        JSONObject json = getCommentJSON();
        try {
            json.put("event_type1", "app_ticket");
            json.put("event_type2", "app_ticket");
            json.put("event_type3", "");
            json.put("event_data", "");
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        send(json, null);
    }

    /***
     *
     * @param type2 只能是TypeConstants.PAY_SUCCESS或TypeConstants.PAY_ORDER
     */
    public void payEvent(PayEvent type2, PayEventBean eventBean) {
        payEvent(type2, eventBean, null);
    }

    /***
     *
     * @param type2 只能是TypeConstants.PAY_SUCCESS或TypeConstants.PAY_ORDER
     * @param listener 网络结果返回 可以是null
     */
    public void payEvent(PayEvent type2, PayEventBean eventBean, OnHttpRequestListener listener) {
        if (!isInit) {
            FengliLog.e(TAG, "payEvent 未初始化");
            return;
        }
        if (httpUtils == null) {
            httpUtils = new HttpUtils();
        }
        JSONObject json = getCommentJSON();
        try {
            json.put("event_type1", "app_pay");
            json.put("event_type2", type2.getConstant());
            json.put("event_type3", eventBean.getEvent());

            JSONObject object = new JSONObject();
            object.put("money", eventBean.getMoney());
            object.put("pay_type", eventBean.getPay_type());
            object.put("order_id", eventBean.getOrder_id());
            object.put("user_id", TextUtils.isEmpty(userID) ? "" : userID);
            json.put("event_data", object.toString());
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        send(json, listener);
        againTime();
    }

    /**
     * @param type2 type2
     * @param type3 type3
     */
    public void adEvent(AdEventOne type2, AdEventTwo type3) {
        adEvent(type2, type3, null);
    }

    /***
     *
     * @param type2 type2
     * @param type3 type3
     * @param listener 网络结果返回 可以是null
     */
    public void adEvent(AdEventOne type2, AdEventTwo type3, OnHttpRequestListener listener) {
        if (!isInit) {
            FengliLog.e(TAG, "adEvent 未初始化");
            return;
        }
        if (httpUtils == null) {
            httpUtils = new HttpUtils();
        }
        JSONObject json = getCommentJSON();
        try {
            json.put("event_type1", "app_ad");
            json.put("event_type2", type2.getConstant());
            json.put("event_type3", type3.getConstant());
            json.put("event_data", "");
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        send(json, listener);
        againTime();
    }

    /**
     * @param type2  自定义事件1
     * @param type3  自定义事件2
     * @param object 附带信息  可以是null
     */
    public void customEvent(String type2, String type3, JSONObject object) {
        customEvent(type2, type3, object, null);
    }

    /**
     * @param type2    自定义事件1
     * @param type3    自定义事件2
     * @param object   附带信息  可以是null
     * @param listener 网络结果返回 可以是null
     */
    public void customEvent(String type2, String type3, JSONObject object, OnHttpRequestListener listener) {
        if (!isInit) {
            FengliLog.e(TAG, "customEvent 未初始化");
            return;
        }
        if (httpUtils == null) {
            httpUtils = new HttpUtils();
        }
        JSONObject json = getCommentJSON();
        try {
            json.put("event_type1", "app_custom");
            json.put("event_type2", type2);
            if (!TextUtils.isEmpty(type3))
                json.put("event_type3", type3);
            if (object != null)
                json.put("event_data", object);
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        send(json, listener);
        againTime();
    }

    private void send(JSONObject json, OnHttpRequestListener listener) {
        if (listener != null) listener.request(json.toString());
        httpUtils.post(json, HOST, new OnHttpRequestListener() {
            @Override
            public void onFail(int code, String msg) {
                if (listener != null) listener.onFail(code, msg);
            }

            @Override
            public void request(String msg) {
            }

            @Override
            public void onSuccess(String result) {
                if (listener != null) listener.onSuccess(result);
            }
        });
    }

    private JSONObject getCommentJSON() {
        initWifeChange();
       /* if (TextUtils.isEmpty(onlyID))
            onlyID = DeviceUtils.getOnlyID(context);*/
        JSONObject json = new JSONObject();
        try {
            json.put("app", appKey);
            json.put("channel", mChannel);
            json.put("device_id", !TextUtils.isEmpty(onlyID) ? onlyID : "");
            if (isChangeInfo || isChangeIP) {
                isChangeInfo = false;
                isChangeIP = false;

                json.put("device", getInfo(context));
            }
        } catch (JSONException e) {
            FengliLog.e(TAG, e);
        }
        return json;
    }

    @SuppressLint("MissingPermission")
    private JSONObject getInfo(Context context) {
        JSONObject object = new JSONObject();
        if (context == null) return object;
        try {
            long time = getPrefs().getLong("init_time", 0);
            if (time == 0) {
                time = System.currentTimeMillis() / 1000;
                getPrefs().edit().putLong("init_time", time).apply();
            }

            object.put("app_version", DeviceUtils.getAppVersionName(context));
            object.put("ipv6", !TextUtils.isEmpty(ipv6) ? ipv6 : "");
            object.put("os_type", 2);
            object.put("imei", "");
            object.put("oaid", !TextUtils.isEmpty(oaid) ? oaid : DeviceUtils.getOAID(context));
            object.put("androidid", DeviceUtils.getAndroidId(context));
            object.put("app_init_time", time);
            object.put("sys_version", Build.VERSION.SDK_INT + "");
            object.put("sys_model", Build.MODEL);
            object.put("sys_brand", Build.BRAND);
            object.put("sys_ua", System.getProperty("http.agent"));
            object.put("sys_screen", DeviceUtils.getWindowScreen(context));
            object.put("sys_mac", DeviceUtils.getMacAddress(context));
            object.put("idfa", "");
            object.put("sys_op_time", "");
            object.put("sys_up_time", "");
            object.put("sys_init_time", "");
            object.put("user_id", TextUtils.isEmpty(userID) ? "" : userID);
        } catch (Exception e) {
            FengliLog.e(TAG, e);
        }
        return object;
    }

    //ip地址是否变化
    private void initWifeChange() {
        if (TextUtils.isEmpty(ipv6)) {
            String ip = DeviceUtils.getIpV6();
            isChangeIP = !TextUtils.equals(ipv6, ip);
            ipv6 = ip;
        }
    }

    public void stopTimer() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    //计时器
    private void time() {
        stopTimer();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = this::ticket;
        scheduledExecutorService.scheduleAtFixedRate(runnable, 30, 30, TimeUnit.SECONDS);
    }

    //防止切换到后台挂了，要重启
    public void againTime() {
        if (scheduledExecutorService != null) {
            if (scheduledExecutorService.isShutdown() || scheduledExecutorService.isTerminated()) {
                time();
            }
        } else {
            time();
        }
    }
}
