/**
 * Bmob移动后端云服务RestAPI工具类
 *
 *
 * 提供简单的RestAPI增删改查工具，可直接对表、云函数、支付订单、消息推送进行操作。
 * 使用方法：先初始化initBmob，后调用其他方法即可。
 * 具体使用方法及传参格式详见Bmob官网RestAPI开发文档。
 * http://docs.bmob.cn/restful/developdoc/index.html?menukey=develop_doc&key=develop_restful
 *
 * @author 金鹰
 * @version V1.3.1
 * @since 2015-07-07
 */
package com.ue.recommend.util

import android.text.TextUtils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BmobUtils private constructor() {

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
        }
    })

    /**
     * 初始化Bmob
     *
     * @param appId  填写 Application ID
     * @param apiKey 填写 REST API Key
     * @return 注册结果
     */
    fun initBmob(appId: String, apiKey: String): Boolean {
        APP_ID = appId
        REST_API_KEY = apiKey
        if (!TextUtils.isEmpty(APP_ID) && !TextUtils.isEmpty(REST_API_KEY)) {
            IS_INIT = true
        }
        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            IS_INIT = false
        }

        return IS_INIT
    }

    /**
     * BQL查询表记录
     *
     * @param BQL   SQL语句。例如：select * from Student where name=? limit ?,? order by name
     * @param value 参数对应SQL中?以,为分隔符。例如"\"张三\",0,10"
     * @return JSON格式结果
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun findBQL(BQL: String, value: String = STRING_EMPTY): String {
        var BQL = BQL
        if (!IS_INIT) {
            return "Unregistered"
        }
        val result: String
        BQL = urlEncoder(BQL) + "&values=[" + urlEncoder(value) + "]"
        val mURL = "https://api.bmob.cn/1/cloudQuery?bql=" + BQL

        val conn = getBmobConnection(URL(mURL), METHOD_GET)
        conn.connect()
        result = getResultFromConnection(conn)
        conn.disconnect()

        return result
    }

    @Throws(IOException::class)
    private fun getResultFromConnection(conn: HttpURLConnection): String {
        val result = StringBuffer()
        val reader = BufferedReader(InputStreamReader(conn.inputStream, UTF8))
        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null) break
            result.append(line)
        }
        reader.close()
        return result.toString()
    }

    @Throws(IOException::class)
    private fun getBmobConnection(url: URL, method: String): HttpURLConnection {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.doInput = true
        conn.readTimeout = TIME_OUT

        conn.useCaches = false
        conn.instanceFollowRedirects = true

        conn.setRequestProperty(BMOB_APP_ID_TAG, APP_ID)
        conn.setRequestProperty(BMOB_REST_KEY_TAG, REST_API_KEY)

        conn.setRequestProperty(CONTENT_TYPE_TAG, CONTENT_TYPE_JSON)
        return conn
    }

    private fun urlEncoder(str: String): String {
        try {
            return URLEncoder.encode(str, UTF8)
        } catch (e1: UnsupportedEncodingException) {
            return str
        }

    }

    companion object {
        private var IS_INIT: Boolean = false
        private var APP_ID: String? = null
        private var REST_API_KEY: String? = null

        private val TIME_OUT = 10000
        private val BMOB_APP_ID_TAG = "X-Bmob-Application-Id"
        private val BMOB_REST_KEY_TAG = "X-Bmob-REST-API-Key"

        private val STRING_EMPTY = ""
        private val UTF8 = "UTF-8"
        private val CONTENT_TYPE_TAG = "Content-Type"
        private val CONTENT_TYPE_JSON = "application/json"
        private val CONTENT_TYPE_TEXT = "text/plain"
        private val METHOD_GET = "GET"

        @Volatile
        var bInstance: BmobUtils? = null

        fun getInstance(): BmobUtils {
            if (bInstance == null) {
                synchronized(BmobUtils::class.java) {
                    if (bInstance == null) bInstance = BmobUtils()
                }
            }
            return bInstance!!
        }
    }
}
