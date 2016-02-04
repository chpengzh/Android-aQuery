package com.chpengzh.app.mini;

import android.app.Application;

import com.android.volley.VolleyError;

import org.aquery.core.annotation.method.Handler;
import org.aquery.core.annotation.method.Validator;
import org.aquery.core.annotation.param.Headers;
import org.aquery.core.annotation.param.NetError;
import org.aquery.core.annotation.param.Params;
import org.aquery.core.annotation.param.RequestEntity;
import org.aquery.core.annotation.param.RequestID;
import org.aquery.core.annotation.param.ResponseBody;
import org.aquery.core.api.Ajax;
import org.aquery.core.AQuery;
import org.aquery.core.Function;

import java.util.Map;

/**
 * @author chpengzh <chpengzh@foxmail.com>
 */
public class App extends Application {

    AQuery $;

    @Override
    public void onCreate() {
        super.onCreate();
        $ = AQuery.getInstance(this)
                .beforeStart(new Function() {
                    @Handler
                    public void before(@Params Map<String, String> params,
                                       @Headers Map<String, String> headers) {
                        params.put("before", "static_param");
                        headers.put("before", "static_header");
                    }
                })
                .validate(new Function() {
                    class BaseResponse {
                        public int status;
                        public String message;
                    }

                    @Validator
                    public void validate(@ResponseBody BaseResponse response) throws VolleyError {
                        if (response == null) {
                            throw new VolleyError("Can't anti-serialize response!");
                        }
                        if (response.status != 200) {
                            throw new VolleyError(response.message);
                        }
                    }
                })
                .beforeSend(new Function() {
                    @Handler
                    public void send(@RequestEntity Ajax ajax, @RequestID long id) {
                        $.log.i("= Request(%d) => \n%s", id, ajax);
                    }
                })
                .beforeDone(new Function() {
                    @Handler
                    public void done(@RequestID long id, @ResponseBody String responseBody) {
                        $.log.i("<= Response(%d) = \n%s", id, responseBody);
                    }
                })
                .beforeError(new Function() {
                    @Handler
                    public void error(@RequestID long id, @NetError int status,
                                      @NetError VolleyError error) {
                        $.log.e("x= Response(%d) = \n Fail for error :%s", id, error);
                        switch (status) {
                            case NetError.CONN_FAIL:
                                $.alert("网络超时或无网络");
                                break;
                            case NetError.VALIDATE:
                                $.alert("校验失败");
                                break;
                        }
                    }
                })
                .afterComplete(new Function() {
                    @Handler
                    public void complete() {
                        $.log.i("---------------------------------------------------");
                    }
                });
        $.config.REQUEST_PREFIX = "http://120.24.68.254";
    }
}
