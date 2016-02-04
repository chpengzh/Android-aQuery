package org.aquery.core.api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public interface Dictionary {

    Dictionary with(String key, Object value);

    Dictionary with(Map<String, ?> map);

    class Impl implements Dictionary {

        private Map<String, Object> entity = new HashMap<>();

        @Override
        public Impl with(String key, Object value) {
            entity.put(key, value);
            return this;
        }

        @Override
        public Impl with(Map<String, ?> map) {
            if (map != null) {
                entity.putAll(map);
            }
            return this;
        }

        @Override
        public String toString() {
            return new Gson().toJson(entity);
        }
    }
}