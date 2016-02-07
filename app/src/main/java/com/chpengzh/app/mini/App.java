package com.chpengzh.app.mini;

import android.app.Application;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.android.volley.VolleyError;

import org.aquery.core.AQuery;
import org.aquery.core.Function;
import org.aquery.core.annotation.method.Handler;
import org.aquery.core.annotation.method.Validator;
import org.aquery.core.annotation.param.Headers;
import org.aquery.core.annotation.param.NetError;
import org.aquery.core.annotation.param.RequestEntity;
import org.aquery.core.annotation.param.RequestID;
import org.aquery.core.annotation.param.ResponseBody;
import org.aquery.core.api.Ajax;

import java.util.Map;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public class App extends Application {

    AQuery $;
    String IMEI;

    @Override
    public void onCreate() {
        super.onCreate();
        IMEI = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        $ = AQuery.getInstance(this)
                .beforeStart(new Function() {
                    @Handler
                    public void before(@Headers Map<String, String> sHeaders) {
                        sHeaders.put("X-AppType", "Android");
                        sHeaders.put("X-DeviceId", IMEI);
                        sHeaders.put("X-RTVersion", Build.VERSION.RELEASE);
                        sHeaders.put("X-ROM", Build.DISPLAY);
                    }
                })
                .validate(new Function() {
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
                        //发送之前, 输出发送信息
                        $.log.i("= Request(%d) => \n%s", id, ajax);
                    }
                })
                .beforeDone(new Function() {
                    @Handler
                    public void done(@RequestID long id, @ResponseBody String responseBody) {
                        //请求成功, 输出响应
                        $.log.i("<= Response(%d) = \n%s", id, responseBody);
                    }
                })
                .beforeError(new Function() {
                    @Handler
                    public void error(@RequestID long id, @NetError int status,
                                      @NetError VolleyError error) {
                        //请求失败, 输出网络错误原因
                        $.log.e("x= Response(%d) = \n Fail for error :", id);
                        $.log.e(error);
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
                        //do nothing
                    }
                });
        $.config.REQUEST_PREFIX = "http://www.aquery.org/api/v1";
    }
}
