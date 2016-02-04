package org.aquery.core.annotation.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetError {

    int NONE = 0;
    int CONN_FAIL = -1;
    int AUTH_FAILURE_ERROR = 401;
    int SERVER_ERROR = 400;
    int NETWORK_ERROR = 500;
    int VALIDATE = 1;

}
