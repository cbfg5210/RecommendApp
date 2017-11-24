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
package com.ue.recommend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

    private static boolean IS_INIT = false;
    private static int TIME_OUT = 10000;

    private static String STRING_EMPTY = "";
    private static String APP_ID = STRING_EMPTY;
    private static String REST_API_KEY = STRING_EMPTY;
    private static String MASTER_KEY = STRING_EMPTY;

    private static final String BMOB_APP_ID_TAG = "X-Bmob-Application-Id";
    private static final String BMOB_REST_KEY_TAG = "X-Bmob-REST-API-Key";
    private static final String BMOB_MASTER_KEY_TAG = "X-Bmob-Master-Key";
    private static final String CONTENT_TYPE_TAG = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String METHOD_GET = "GET";

    private static final String UTF8 = "UTF-8";
    private static final String CHAR_RISK = ":";

    public static final String MSG_NOT_FOUND = "Not Found";
    public static final String MSG_ERROR = "Error";
    public static final String MSG_UNREGISTERED = "Unregistered";

    /**
     * 是否初始化Bmob
     *
     * @return 初始化结果
     */
    public static boolean isInit() {
        return IS_INIT;
    }

    /**
     * 初始化Bmob
     *
     * @param appId  填写 Application ID
     * @param apiKey 填写 REST API Key
     * @return 注册结果
     */
    public static boolean initBmob(String appId, String apiKey) {
        return initBmob(appId, apiKey, 10000);
    }

    /**
     * 初始化Bmob
     *
     * @param appId   填写 Application ID
     * @param apiKey  填写 REST API Key
     * @param timeout 设置超时（1000~20000ms）
     * @return 注册结果
     */
    public static boolean initBmob(String appId, String apiKey, int timeout) {
        APP_ID = appId;
        REST_API_KEY = apiKey;
        if (!APP_ID.equals(STRING_EMPTY) && !REST_API_KEY.equals(STRING_EMPTY)) {
            IS_INIT = true;
        }
        if (timeout > 1000 && timeout < 20000) {
            TIME_OUT = timeout;
        }
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            IS_INIT = false;
        }
        return isInit();
    }

    /**
     * BQL查询表记录
     *
     * @param BQL SQL语句。例如：select * from Student where name=\"张三\" limit 0,10 order by name
     * @return JSON格式结果
     */
    public static String findBQL(String BQL) {
        return findBQL(BQL, STRING_EMPTY);
    }

    /**
     * BQL查询表记录
     *
     * @param BQL   SQL语句。例如：select * from Student where name=? limit ?,? order by name
     * @param value 参数对应SQL中?以,为分隔符。例如"\"张三\",0,10"
     * @return JSON格式结果
     */
    public static String findBQL(String BQL, String value) {
        String result = STRING_EMPTY;
        if (isInit()) {
            HttpURLConnection conn = null;
            BQL = urlEncoder(BQL) + "&values=[" + urlEncoder(value) + "]";
            String mURL = "https://api.bmob.cn/1/cloudQuery?bql=" + BQL;

            try {
                conn = connectionCommonSetting(conn, new URL(mURL), METHOD_GET);
                conn.connect();
                result = getResultFromConnection(conn);
                conn.disconnect();
            } catch (FileNotFoundException e) {
                result = MSG_NOT_FOUND + CHAR_RISK + "(findBQL)" + e.getMessage();
            } catch (Exception e) {
                result = MSG_ERROR + CHAR_RISK + "(findBQL)" + e.getMessage();
            }
        } else {
            result = MSG_UNREGISTERED;
        }
        return result;
    }

    public static int getTimeout() {
        return TIME_OUT;
    }

    public static void setTimeout(int timeout) {
        TIME_OUT = timeout;
    }

    private static void printWriter(HttpURLConnection conn, String paramContent) throws UnsupportedEncodingException, IOException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), UTF8));
        out.write(paramContent);
        out.flush();
        out.close();
    }

    private static String getResultFromConnection(HttpURLConnection conn) throws UnsupportedEncodingException, IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF8));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }

    private static HttpURLConnection connectionCommonSetting(HttpURLConnection conn, URL url, String method) throws IOException {
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setReadTimeout(TIME_OUT);

        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);

        conn.setRequestProperty(BMOB_APP_ID_TAG, APP_ID);
        conn.setRequestProperty(BMOB_REST_KEY_TAG, REST_API_KEY);
        if (!MASTER_KEY.equals(STRING_EMPTY)) {
            conn.setRequestProperty(BMOB_MASTER_KEY_TAG, MASTER_KEY);
        }

        conn.setRequestProperty(CONTENT_TYPE_TAG, CONTENT_TYPE_JSON);
        return conn;
    }

    private static TrustManager[] trustAllCerts = new TrustManager[]{
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

    private static String urlEncoder(String str) {
        try {
            return URLEncoder.encode(str, UTF8);
        } catch (UnsupportedEncodingException e1) {
            return str;
        }
    }
}
