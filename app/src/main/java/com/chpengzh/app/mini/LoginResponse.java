package com.chpengzh.app.mini;

import org.aquery.core.api.Checker;
import org.aquery.core.api.Validator;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public class LoginResponse extends BaseResponse implements Checker {
    public String token;

    @Override
    public Validator getValidator() {
        return Validator.newInstance().filter(token == null || token.length() > 5, "illegal token");
    }
}
