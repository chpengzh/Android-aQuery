package org.aquery.core.api;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.aquery.core.AQuery;
import org.aquery.core.Function;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public interface Ajax {

    int _START = 0;
    int START = 1;
    int _SEND = 2;
    int VALIDATE = 3;
    int _DONE = 4;
    int DONE = 5;
    int _FAIL = 6;
    int FAIL = 7;
    int COMPLETE = 8;
    int _COMPLETE = 9;

    /***
     * static Handler Container
     */
    Map<Integer, Function> sHandler = new HashMap<>();

    /***
     * Add a http request query param
     *
     * @param key   query param key
     * @param value query param value
     * @return Ajax instance
     */
    Ajax param(String key, String value);

    /***
     * Add a http request header
     *
     * @param key   header key
     * @param value header value
     * @return Ajax instance
     */
    Ajax header(String key, String value);

    /***
     * Set http post body
     *
     * @param body body as byte[]
     * @return Ajax instance
     */
    Ajax body(byte[] body);

    /***
     * Set http post body
     *
     * @param dict body as Dictionary
     * @return Ajax instance
     */
    Ajax body(Dictionary dict);

    /***
     * Set http post body
     *
     * @param str body as String
     * @return Ajax instance
     */
    Ajax body(String str);

    /***
     * Set http success handler
     *
     * @param func handler which will be called in DONE
     * @return Ajax instance
     */
    Ajax done(Function func);

    /***
     * Set http fail handler
     *
     * @param func handler which will be called in FAIL
     * @return Ajax instance
     */
    Ajax fail(Function func);

    /***
     * Set http handler when request is start
     *
     * @param func handler which will be called in START
     * @return Ajax instance
     */
    Ajax start(Function func);

    /***
     * Set http handler when request is finish, whenever it is success or fail
     *
     * @param func handler which will be called in COMPLETE
     * @return Ajax instance
     */
    Ajax always(Function func);

    /***
     * Execute Http request which is defined in Ajax instance
     *
     * @return Volley Request instance
     */
    Request<?> send();

    /***
     * Execute Http request which is defined in Ajax instance
     *
     * @return Volley Request instance, which is bind with a certain tag
     */
    Request<?> send(Object tag);

    /***
     * Get Ajax defined url
     *
     * @return url String
     */
    String getURL();

    /***
     * Get Ajax defined method
     *
     * @return Constant integer defined in Request of volley(Method.GET/Method.POST)
     */
    int getMethod();

    /***
     * Get Ajax defined query parameters
     *
     * @return Reference of Query Parameters Map
     */
    Map<String, String> getParams();

    /***
     * Get Ajax defined query parameters
     *
     * @return Reference of Header Key-Value Map
     */
    Map<String, String> getHeaders();

    /***
     * Get Request Body
     *
     * @return request Body as byte[]
     */
    byte[] getRequestBody();

    /***
     * Get Response Body
     *
     * @return response Body as byte[]
     */
    byte[] getResponseBody();

    /***
     * Get Response Error
     *
     * @return response Volley as VolleyError
     */
    VolleyError getResponseError();

    /***
     * Get Request
     *
     * @return request as volley Request
     */
    Request<?> getRequestEntity();

    class Impl implements Ajax {

        private AQuery $;
        private final Map<Integer, Function> mHandler = new HashMap<>();
        private final int mMethod;
        private final String mURL;
        private final Map<String, String> mHeaders = new HashMap<>();
        private final Map<String, String> mQueryParam = new HashMap<>();
        private byte[] mReqBody;
        private byte[] mResponseByte;
        private VolleyError mError;
        private Request<byte[]> mRequest;

        public Impl(AQuery $, int method, String url) {
            this.$ = $;
            this.mMethod = method;
            this.mURL = url;
        }

        @Override
        public final Ajax param(String key, String value) {
            try {
                mQueryParam.put(key, URLEncoder.encode(value, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                $.log.e(e);
            }
            return this;
        }

        @Override
        public final Ajax header(String key, String value) {
            mHeaders.put(key, value);
            return this;
        }

        @Override
        public final Ajax body(byte[] body) {
            mReqBody = body;
            return this;
        }

        @Override
        public final Ajax body(Dictionary dict) {
            this.mReqBody = dict.toString().getBytes();
            return this;
        }

        @Override
        public final Ajax body(String str) {
            this.mReqBody = str.getBytes();
            return this;
        }

        @Override
        public final Ajax done(Function func) {
            mHandler.put(DONE, func);
            return this;
        }

        @Override
        public final Ajax fail(Function func) {
            mHandler.put(FAIL, func);
            return this;
        }

        @Override
        public final Ajax start(Function func) {
            mHandler.put(START, func);
            return this;
        }

        @Override
        public final Ajax always(Function func) {
            mHandler.put(COMPLETE, func);
            return this;
        }

        @Override
        public Request send() {
            return send($.getContext());
        }

        @Override
        public Request send(Object tag) {
            getHandler(_START).callHandler($, this);
            getHandler(START).callHandler($, this);
            getHandler(_SEND).callHandler($, this);
            mRequest = new AjaxRequest();
            mRequest.setTag(tag);
            $.getRequestQueue().add(mRequest);
            return mRequest;
        }

        @Override
        public String getURL() {
            return mURL;
        }

        @Override
        public int getMethod() {
            return mMethod;
        }

        @Override
        public Map<String, String> getParams() {
            return mQueryParam;
        }

        @Override
        public Map<String, String> getHeaders() {
            return mHeaders;
        }

        @Override
        public byte[] getRequestBody() {
            return mReqBody;
        }

        @Override
        public byte[] getResponseBody() {
            return mResponseByte;
        }

        @Override
        public VolleyError getResponseError() {
            return mError;
        }

        @Override
        public Request<byte[]> getRequestEntity() {
            return mRequest;
        }

        private Function getHandler(int life) {
            if (mHandler.get(life) != null) return mHandler.get(life);
            return sHandler.get(life) == null ? new Function() : sHandler.get(life);
        }

        @Override
        public String toString() {
            try {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                return String.format("[url]%s\n[method]%s\n[header]\n%s[body]\n%s\n",
                        mURL, mMethod == Request.Method.GET ? "GET" : "POST", sb.toString(),
                        new String(mReqBody == null ? "<null data>".getBytes() : mReqBody, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                $.log.e(e);
                return super.toString();
            }
        }

        private class AjaxRequest extends Request<byte[]> {
            public AjaxRequest() {
                super(mMethod, mURL, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        getHandler(_FAIL).callHandler($, Impl.this);
                        getHandler(FAIL).callHandler($, Impl.this);
                        //----
                        getHandler(COMPLETE).callHandler($, Impl.this);
                        getHandler(_COMPLETE).callHandler($, Impl.this);
                    }
                });
                setRetryPolicy(new DefaultRetryPolicy($.config.REQUEST_TIME_OUT,
                        $.config.REQUEST_RETRY, DefaultRetryPolicy.DEFAULT_MAX_RETRIES));
                header("Content-Type", "application/json");
            }

            @Override
            public String getUrl() {
                StringBuilder query = new StringBuilder();
                for (Map.Entry<String, String> entry : mQueryParam.entrySet()) {
                    query.append(entry.getKey()).append("=")
                            .append(entry.getValue()).append("&");
                }
                if (query.toString().endsWith("&")) {
                    query.setLength(query.length() - 1);
                }
                return query.length() == 0 ? super.getUrl() :
                        String.format("%s?%s", super.getUrl(), query.toString());
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return mHeaders;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return mReqBody;
            }

            @Override
            protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                try {
                    mResponseByte = response.data;
                    getHandler(VALIDATE).callValidator($, Impl.this);
                    return Response.success(response.data, parseCacheHeaders(response));
                } catch (VolleyError error) {
                    return Response.error(error);
                }
            }

            @Override
            protected void deliverResponse(byte[] response) {
                getHandler(_DONE).callHandler($, Impl.this);
                getHandler(DONE).callHandler($, Impl.this);
                //------
                getHandler(COMPLETE).callHandler($, Impl.this);
                getHandler(_COMPLETE).callHandler($, Impl.this);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                mError = super.parseNetworkError(volleyError);
                return mError;
            }
        }
    }
}