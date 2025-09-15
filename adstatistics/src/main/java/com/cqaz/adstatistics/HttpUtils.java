package com.cqaz.adstatistics;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HttpUtils {
    @SuppressLint("StaticFieldLeak")
    public void post(JSONObject json, String uri, @Nullable OnHttpRequestListener listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... a) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(uri);
                    connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setInstanceFollowRedirects(true);
                    connection.setConnectTimeout(5000);

                    connection.connect();
                    String params = json.toString();
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(params.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    outputStream.close();
                    FengliLog.e(FengliAdSDK.TAG, params);
                    int code = connection.getResponseCode();
                    if (code == 200) {//成功
                        InputStream inputStream = connection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        BufferedReader buffer = new BufferedReader(reader);
                        StringBuilder msg = new StringBuilder();
                        String line;
                        while ((line = buffer.readLine()) != null) {
                            msg.append(line);
                        }
                        FengliLog.e(FengliAdSDK.TAG, msg.toString());
                        reader.close();
                        buffer.close();

                        if (listener != null) listener.onSuccess(msg.toString());
                    } else {
                        if (code == 401) {//app名不对
                            if (listener != null) listener.onFail(code, "appKey错误");
                        } else if (code == 500) {//传参数错误
                            if (listener != null) listener.onFail(code, "传参数错误");
                        } else {//其他未知错误
                            if (listener != null) listener.onFail(code, connection.getResponseMessage());
                        }
                    }
                } catch (Exception e) {
                    FengliLog.e(FengliAdSDK.TAG, e);
                    if (listener != null) listener.onFail(408, "");
                } finally {
                    if (connection != null) connection.disconnect();
                }

                return "";
            }

            @Override
            protected void onPostExecute(String result) {
            }
        }.execute();
    }
}
