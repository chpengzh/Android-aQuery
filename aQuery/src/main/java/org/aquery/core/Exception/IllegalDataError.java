package org.aquery.core.exception;

import com.android.volley.VolleyError;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public class IllegalDataError extends VolleyError {

    public IllegalDataError(String s) {
        super(s);
    }
}
