package com.cqaz.adstatistics;

public interface OnHttpRequestListener {
    void onFail(int code, String msg);

    void request(String msg);

    void onSuccess(String result);
}
