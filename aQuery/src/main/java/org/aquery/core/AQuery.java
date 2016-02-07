package org.aquery.core;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import org.aquery.core.api.Ajax;
import org.aquery.core.api.Checker;
import org.aquery.core.api.Dictionary;
import org.aquery.core.api.Logger;
import org.aquery.core.api.Reflector;
import org.aquery.core.api.Validator;
import org.aquery.core.exception.IllegalDataError;

import java.io.File;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public abstract class AQuery {

    /***
     * public config
     */
    public Config config;

    /***
     * logger
     */
    public Logger log;

    /************************************************************************
     * Get singleton instance of aQuery
     *
     * @param context context
     * @return aQuery instance
     ************************************************************************/
    public static AQuery getInstance(Context context) {
        if (Impl.sInstance == null) {
            Impl.sInstance = new Impl(context);
        }
        return Impl.sInstance;
    }

    /***
     * Get the application context where aQuery based
     *
     * @return context
     */
    public abstract Context getContext();

    /***
     * Register a handler to be called before start.
     *
     * @param func handler which will be called while _START
     * @return this aQuery instance
     */
    public abstract AQuery beforeStart(Function func);

    /***
     * Register a handler to be called before send.
     *
     * @param func handler which will be called while _SEND
     * @return this aQuery instance
     */
    public abstract AQuery beforeSend(Function func);

    /***
     * Attach a function to be executed whenever an Ajax request completes successfully.
     *
     * @param func handler which will be called while _DONE
     * @return this aQuery instance
     */
    public abstract AQuery beforeDone(Function func);

    /***
     * Register a handler to be called when Ajax requests complete with an error.
     *
     * @param func error handler which will be called while _FAIL
     * @return this aQuery instance
     */
    public abstract AQuery beforeError(Function func);

    /***
     * Register a handler to be called when Ajax requests complete.
     *
     * @param func handler will be called while _COMPLETE
     * @return this aQuery instance
     */
    public abstract AQuery afterComplete(Function func);

    /***
     * Register a handler to be called when receive a response.
     *
     * @param func handler will be called while VALIDATE
     * @return this aQuery instance
     */
    public abstract AQuery validate(Function func);

    /***
     * cancel request by tag
     *
     * @param tags tags of request
     * @return AQuery instance
     */
    public abstract AQuery cancel(Object... tags);

    /***
     * cancel request by filter
     *
     * @param filters filters of request
     * @return AQuery instance
     */
    public abstract AQuery cancel(RequestQueue.RequestFilter... filters);

    /***
     * Create a Ajax GET request builder
     *
     * @param url request url
     * @return Ajax GET request builder, which analysis response as String
     */
    public abstract Ajax get(String... url);

    /***
     * Create a Ajax POST request builder
     *
     * @param url request url
     * @return Ajax POST request builder, which analysis response as String
     */
    public abstract Ajax post(String... url);

    /***
     * Get volley request queue
     *
     * @return request queue instance
     */
    public abstract RequestQueue getRequestQueue();

    /***
     * Dictionary/map Model creator
     *
     * @return Dictionary instance
     */
    public abstract Dictionary dict();

    /***
     * Toast message
     *
     * @param message message to be toast
     */
    public abstract void alert(String message, Object... args);

    /***
     * Reflect a object source
     *
     * @param src object to be reflected
     * @return Reflector of an object
     */
    public abstract Reflector load(Object src);

    /***
     * Reflect a json with it's class type
     *
     * @param json json String
     * @param type load type
     * @return Reflector fo an object
     */
    public abstract Reflector loadJson(String json, Class type);

    /***
     * validate a checker follow its validate principles
     *
     * @param checker checker instance
     * @return whether it has any warning message
     */
    public abstract boolean validate(Checker checker) throws IllegalDataError;

    static class Impl extends AQuery {

        static AQuery sInstance;
        private Context mContext;
        RequestQueue mRequestQueue;

        Impl(Context context) {
            this.mContext = context.getApplicationContext();
            //public member
            this.config = new Config();
            this.log = new Logger.Impl(this);

            VolleyLog.DEBUG = false;
            //network queue and cache
            Cache cache = new DiskBasedCache(new File(mContext.getCacheDir(),
                    config.VOLLEY_CACHE_DIR), 30 * 1024 * 1024); // 30MB tops
            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());
            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);
            // Start the queue
            mRequestQueue.start();
        }

        @Override
        public Context getContext() {
            return mContext;
        }

        @Override
        public AQuery beforeStart(Function func) {
            Ajax.sHandler.put(Ajax._START, func);
            return this;
        }

        @Override
        public AQuery beforeSend(Function func) {
            Ajax.sHandler.put(Ajax._SEND, func);
            return this;
        }

        @Override
        public AQuery beforeDone(Function func) {
            Ajax.sHandler.put(Ajax._DONE, func);
            return this;
        }

        @Override
        public AQuery beforeError(Function func) {
            Ajax.sHandler.put(Ajax._FAIL, func);
            return this;
        }

        @Override
        public AQuery afterComplete(Function func) {
            Ajax.sHandler.put(Ajax._COMPLETE, func);
            return this;
        }

        @Override
        public AQuery validate(Function func) {
            Ajax.sHandler.put(Ajax.VALIDATE, func);
            return this;
        }

        @Override
        public AQuery cancel(Object... tags) {
            for (Object tag : tags) mRequestQueue.cancelAll(tag);
            return this;
        }

        @Override
        public AQuery cancel(RequestQueue.RequestFilter... filters) {
            for (RequestQueue.RequestFilter filter : filters) mRequestQueue.cancelAll(filter);
            return this;
        }

        @Override
        public Ajax get(String... url) {
            StringBuilder sb = new StringBuilder(url.length > 0 && url[0] != null && url[0]
                    .toLowerCase().startsWith("http://") ? "" : config.REQUEST_PREFIX);
            for (String path : url) {
                sb.append(path);
            }
            return new Ajax.Impl(this, Request.Method.GET, sb.toString());
        }

        @Override
        public Ajax post(String... url) {
            StringBuilder sb = new StringBuilder(url.length > 0 && url[0] != null && url[0]
                    .toLowerCase().startsWith("http://") ? "" : config.REQUEST_PREFIX);
            for (String path : url) {
                sb.append(path);
            }
            return new Ajax.Impl(this, Request.Method.POST, sb.toString());
        }

        @Override
        public RequestQueue getRequestQueue() {
            return this.mRequestQueue;
        }

        @Override
        public Dictionary dict() {
            return new Dictionary.Impl();
        }

        @Override
        public void alert(String message, Object... args) {
            Toast.makeText(mContext, args.length == 0 ? message : String.format(message, args),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public Reflector load(Object src) {
            return new Reflector.Impl(this, src);
        }

        @Override
        public Reflector loadJson(String json, Class type) {
            return new Reflector.Impl(this, json, type);
        }

        @Override
        public boolean validate(Checker checker) throws IllegalDataError {
            if (checker == null) throw new IllegalDataError("null checker");
            if (checker.getValidator() == null) throw new IllegalDataError("null validator");
            return checker.getValidator().validate();
        }
    }
}