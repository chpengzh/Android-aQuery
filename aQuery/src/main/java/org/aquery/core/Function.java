package org.aquery.core;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.aquery.core.annotation.method.Handler;
import org.aquery.core.annotation.method.Validator;
import org.aquery.core.annotation.param.*;
import org.aquery.core.annotation.param.Method;
import org.aquery.core.api.Ajax;
import org.aquery.core.api.Checker;
import org.aquery.core.exception.IllegalDataError;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ajax Request handler function
 * Support Annotation method:
 * - Handler.class for user define action handler
 * - Checker.class for response getValidator handler
 *
 * @author chpengzh chpengzh@foxmail.com
 */
public class Function {

    public void callHandler(final AQuery $, final Ajax ajax) {
        Map<Annotation, java.lang.reflect.Method> method = ReflectUtils
                .getMethodsByAnnotation(Handler.class, getClass());
        for (Map.Entry<Annotation, java.lang.reflect.Method> entry : method.entrySet()) {
            try {
                entry.getValue().invoke(this, ReflectUtils.fillParamsByAnnotations(entry.getValue(),
                        new ReflectUtils.ParamInjector() {
                            @Override
                            public Object onInject(Class paramType, List<? extends Annotation> annotations,
                                                   int position) {
                                try {
                                    return scanAnnotation($, paramType, annotations.get(0), ajax);
                                } catch (Exception e) {
                                    return null;
                                }
                            }
                        }));
            } catch (Exception e) {
                $.log.i(e);
            }
        }
    }

    public void callValidator(final AQuery $, final Ajax ajax) throws VolleyError {
        Map<Annotation, java.lang.reflect.Method> method = ReflectUtils
                .getMethodsByAnnotation(Handler.class, getClass());
        for (Map.Entry<Annotation, java.lang.reflect.Method> entry : method.entrySet()) {
            try {
                entry.getValue().invoke(this, ReflectUtils.fillParamsByAnnotations(entry.getValue(),
                        new ReflectUtils.ParamInjector() {
                            @Override
                            public Object onInject(Class paramType, List<? extends Annotation> annotations,
                                                   int position) {
                                try {
                                    return scanAnnotation($, paramType, annotations.get(0), ajax);
                                } catch (Exception e) {
                                    $.log.i(e);
                                    return null;
                                }
                            }
                        }));
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof VolleyError) {
                    throw ((VolleyError) e.getTargetException());
                }
                $.log.i(e.getTargetException());
            } catch (Exception e) {
                $.log.i(e);
            }
        }
    }

    public void callModelValidator(final AQuery $, final Ajax ajax) throws VolleyError {
        Map<Annotation, java.lang.reflect.Method> methodMap = ReflectUtils
                .getMethodsByAnnotation(Handler.class, getClass());
        for (Map.Entry<Annotation, java.lang.reflect.Method> entry : methodMap.entrySet()) {
            java.lang.reflect.Method method = entry.getValue();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class type = method.getParameterTypes()[i];
                if (method.getParameterAnnotations()[i][0] instanceof ResponseBody &&
                        type != String.class && type != byte[].class &&
                        Checker.class.isAssignableFrom(type)) {
                    try {
                        Checker checker = (Checker) new Gson()
                                .fromJson(new String(ajax.getResponseBody(), "utf-8"), type);
                        if (checker.getValidator().validate()) {
                            $.log.i("has warning");
                        }
                    } catch (UnsupportedEncodingException e) {
                        $.log.i(e);
                        throw new IllegalDataError("Unsupported encoding bytes");
                    }
                }
            }
        }
    }

    private Object scanAnnotation(AQuery $, Class<?> type, Annotation annotation, Ajax ajax)
            throws Exception {
        if (annotation instanceof Header) {
            return ajax.getHeaders().get(((Header) annotation).value());
        } else if (annotation instanceof Headers) {
            return ajax.getHeaders();
        } else if (annotation instanceof Method) {
            return ajax.getMethod();
        } else if (annotation instanceof NetError) {
            if (type == int.class || type == Integer.class) {
                return getVolleyErrorType(ajax.getResponseError());
            }
            return ajax.getResponseError();
        } else if (annotation instanceof Param) {
            return ajax.getParams().get(((Param) annotation).value());
        } else if (annotation instanceof Params) {
            return ajax.getParams();
        } else if (annotation instanceof RequestBody) {
            return antiSerialize($, type, ajax.getRequestBody());
        } else if (annotation instanceof RequestEntity) {
            if (type == Request.class) return ajax.getRequestEntity();
            if (type == Ajax.class) return ajax;
        } else if (annotation instanceof RequestID) {
            return ajax.hashCode();
        } else if (annotation instanceof ResponseBody) {
            return antiSerialize($, type, ajax.getResponseBody());
        } else if (annotation instanceof URL) {
            return ajax.getURL();
        }
        return null;
    }

    private Object antiSerialize(AQuery $, Class<?> type, byte[] src) {
        try {
            if (type == byte[].class) {
                return src;
            } else if (type == String.class) {
                return new String(src, "utf-8");
            } else {
                return new Gson().fromJson(new String(src, "utf-8"), type);
            }
        } catch (Exception e) {
            $.log.i(e);
            return null;
        }
    }

    /***
     * Get Error code defined in NetError.class
     *
     * @param error volley error
     * @return status code defined in NetError interface
     */
    public int getVolleyErrorType(VolleyError error) {
        if (error == null) return NetError.NONE;
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            return NetError.CONN_FAIL;
        } else if (error instanceof AuthFailureError) {
            return NetError.AUTH_FAILURE_ERROR;
        } else if (error instanceof ServerError) {
            return NetError.SERVER_ERROR;
        } else if (error instanceof NetworkError) {
            return NetError.NETWORK_ERROR;
        } else {
            return NetError.VALIDATE;
        }
    }
}