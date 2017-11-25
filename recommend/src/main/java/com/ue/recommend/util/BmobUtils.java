/**
 * Bmob移动后端云服务RestAPI工具类
 * <p>
 * 提供简单的RestAPI增删改查工具，可直接对表、云函数、支付订单、消息推送进行操作。
 * 使用方法：先初始化initBmob，后调用其他方法即可。
 * 具体使用方法及传参格式详见Bmob官网RestAPI开发文档。
 * http://docs.bmob.cn/restful/developdoc/index.html?menukey=develop_doc&key=develop_restful
 *
 * @author 金鹰
 * @version V1.3.1
 * @since 2015-07-07
 */
package com.ue.recommend.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BmobUtils {
    private static boolean IS_INIT;
    private static String APP_ID;
    private static String REST_API_KEY;

    private static final int TIME_OUT = 10000;
    private static final String BMOB_APP_ID_TAG = "X-Bmob-Application-Id";
    private static final String BMOB_REST_KEY_TAG = "X-Bmob-REST-API-Key";

    private static final String STRING_EMPTY = "";
    private static final String UTF8 = "UTF-8";
    private static final String CONTENT_TYPE_TAG = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT = "text/plain";
    private static final String METHOD_GET = "GET";

    private static volatile BmobUtils instance;

    public static BmobUtils getInstance() {
        if (instance == null) {
            synchronized (BmobUtils.class) {
                if (instance == null) {
                    instance = new BmobUtils();
                }
            }
        }
        return instance;
    }

    private BmobUtils() {
    }

    /**
     * 初始化Bmob
     *
     * @param appId  填写 Application ID
     * @param apiKey 填写 REST API Key
     * @return 注册结果
     */
    public boolean initBmob(String appId, String apiKey) {
        APP_ID = appId;
        REST_API_KEY = apiKey;
        if (!TextUtils.isEmpty(APP_ID) && !TextUtils.isEmpty(REST_API_KEY)) {
            IS_INIT = true;
        }
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            IS_INIT = false;
        }
        return IS_INIT;
    }

    /**
     * BQL查询表记录
     *
     * @param BQL SQL语句。例如：select * from Student where name=\"张三\" limit 0,10 order by name
     * @return JSON格式结果
     */
    public String findBQL(String BQL) throws IOException {
        return findBQL(BQL, STRING_EMPTY);
    }

    /**
     * BQL查询表记录
     *
     * @param BQL   SQL语句。例如：select * from Student where name=? limit ?,? order by name
     * @param value 参数对应SQL中?以,为分隔符。例如"\"张三\",0,10"
     * @return JSON格式结果
     */
    public String findBQL(String BQL, String value) throws IOException {
        if (!IS_INIT) {
            return "Unregistered";
        }
        String result;
        BQL = urlEncoder(BQL) + "&values=[" + urlEncoder(value) + "]";
        String mURL = "https://api.bmob.cn/1/cloudQuery?bql=" + BQL;

        HttpURLConnection conn = getBmobConnection(new URL(mURL), METHOD_GET);
        conn.connect();
        result = getResultFromConnection(conn);
        conn.disconnect();

        return result;
    }

    public String search(String kw) throws IOException {
        String result;
        String mURL = String.format("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_search_result?keyword=%s&platform=touch&network_type=undefined&resolution=720x1080", urlEncoder(kw));

        HttpURLConnection conn = getCommonConnection(new URL(mURL), METHOD_GET);
        conn.connect();
        result = getResultFromConnection(conn);
        conn.disconnect();

        return result;
    }

    private HttpURLConnection getCommonConnection(URL url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setReadTimeout(TIME_OUT);

        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);

        conn.setRequestProperty(CONTENT_TYPE_TAG, CONTENT_TYPE_TEXT);

        return conn;
    }

    private String getResultFromConnection(HttpURLConnection conn) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF8));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }

    private HttpURLConnection getBmobConnection(URL url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setReadTimeout(TIME_OUT);

        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);

        conn.setRequestProperty(BMOB_APP_ID_TAG, APP_ID);
        conn.setRequestProperty(BMOB_REST_KEY_TAG, REST_API_KEY);

        conn.setRequestProperty(CONTENT_TYPE_TAG, CONTENT_TYPE_JSON);
        return conn;
    }

    private TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }
            }
    };

    private String urlEncoder(String str) {
        try {
            return URLEncoder.encode(str, UTF8);
        } catch (UnsupportedEncodingException e1) {
            return str;
        }
    }
}
