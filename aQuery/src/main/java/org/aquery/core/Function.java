package org.aquery.core;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.aquery.core.annotation.method.Handler;
import org.aquery.core.annotation.method.Validator;
import org.aquery.core.annotation.param.*;
import org.aquery.core.annotation.param.Method;
import org.aquery.core.api.Ajax;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Ajax Request handler function
 * Support Annotation method:
 * - Handler.class for user define action handler
 * - Validator.class for response validate handler
 *
 * @author chpengzh chpengzh@foxmail.com
 */
public class Function {

    public void callHandler(AQuery $, Ajax ajax) {
        for (java.lang.reflect.Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(Handler.class)) {
                Object[] params = new Object[method.getParameterTypes().length];
                iterate($, ajax, method, params);
                try {
                    method.invoke(this, params);
                } catch (Exception e) {
                    $.log.e(e);
                }
                break;
            }
        }
    }

    public void callValidator(AQuery $, Ajax ajax) throws VolleyError {
        for (java.lang.reflect.Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(Validator.class)) {
                Object[] params = new Object[method.getParameterTypes().length];
                iterate($, ajax, method, params);
                try {
                    method.invoke(this, params);
                } catch (InvocationTargetException e) {
                    $.log.e(e.getTargetException());
                    if (e.getTargetException() instanceof VolleyError) {
                        throw (VolleyError) e.getTargetException();
                    }
                } catch (Exception e) {
                    $.log.e(e);
                }
                break;
            }
        }
    }

    private void iterate(AQuery $, Ajax ajax, java.lang.reflect.Method method, Object[] params) {
        for (int i = 0; i < params.length; i++) {
            Annotation[] annotations = method.getParameterAnnotations()[i];
            try {
                for (Annotation annotation : annotations) {
                    Object inject = scanAnnotation($, method.getParameterTypes()[i],
                            annotation, ajax);
                    if (inject != null) {
                        params[i] = inject;
                        break;
                    }
                }
            } catch (Exception e) {
                $.log.e(e);
                params[i] = null;
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
            $.log.e(e);
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