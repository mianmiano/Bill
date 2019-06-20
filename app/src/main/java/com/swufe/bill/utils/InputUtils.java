package com.swufe.bill.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class InputUtils {
    private static String TAG = "InputUtils";
    private static String APIKEY = "7e8d189941904e679555a2c0ee1b1813";

    public static String getString(String question){
        String out = null;
        try {
            String info = URLEncoder.encode(question,"utf-8");
            URL url = new URL("http://www.tuling123.com/openapi/api?key="
                    + APIKEY + "&info=" + info);
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
//            connection.setReadTimeout(10_1000);
            if (code == 200){
                InputStream inputStream = connection.getInputStream();
                String result = streamToString(inputStream);
                JSONObject object = new JSONObject(result);
                out = object.getString("text");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    private static String streamToString(InputStream in) {
        String result = "";
        try {
            // 创建一个字节数组写入流
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                out.flush();
            }
            result = new String(out.toByteArray(), "utf-8");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
