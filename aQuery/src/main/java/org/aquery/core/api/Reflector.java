package org.aquery.core.api;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.aquery.core.AQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public interface Reflector {

    /***
     * convert to json string
     *
     * @return json string
     */
    String toJson();

    /***
     * object's field name - property value map
     *
     * @return kv map
     */
    Map<String, Object> toMap();

    /***
     * object's field name set
     *
     * @return field name set
     */
    Set<String> fieldSet();

    /***
     * set property value
     *
     * @param prop  property path string
     * @param value property value
     * @return action success flag
     */
    boolean put(String prop, Object value);

    /***
     * get property value
     *
     * @param prop property path string
     * @return property value
     */
    Object get(String prop);

    /***
     * iterate fields to do something
     *
     * @param action field handle action
     */
    void forEach(Action action);

    /***
     * field handle action
     */
    interface Action {
        void action(Object src, Field field, Object value) throws IllegalAccessException;
    }

    class Impl implements Reflector {

        Object src;
        AQuery $;
        Set<String> mField = new HashSet<>();
        Map<String, Object> mDict = new HashMap<>();

        public Impl(AQuery $, String json, Class type) {
            this.$ = $;
            this.src = new Gson().fromJson(json, type);
            forEach(new Action() {
                @Override
                public void action(Object src, Field field, Object value)
                        throws IllegalAccessException {
                    mField.add(field.getName());
                    mDict.put(field.getName(), value);
                }
            });
        }

        public Impl(AQuery $, Object src) {
            this.$ = $;
            this.src = src;
            forEach(new Action() {
                @Override
                public void action(Object src, Field field, Object value)
                        throws IllegalAccessException {
                    mField.add(field.getName());
                    mDict.put(field.getName(), value);
                }
            });
        }

        @Override
        public String toJson() {
            try {
                return new Gson().toJson(src);
            } catch (JsonIOException error) {
                $.log.i(error);
                return null;
            }
        }

        @Override
        public Map<String, Object> toMap() {
            return mDict;
        }

        @Override
        public Set<String> fieldSet() {
            return mField;
        }

        @Override
        public boolean put(String prop, Object value) {
            List<String> keys = new ArrayList<>();
            Collections.addAll(keys, prop.split("\\."));
            try {
                put(src, keys, value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        private static void put(Object src, List<String> keys, Object value)
                throws NoSuchFieldException, IllegalAccessException {
            if (keys.size() == 0 || src == null) {
                //do nothing
            } else if (keys.size() == 1) {
                Field field = src.getClass().getField(keys.remove(0));
                field.setAccessible(true);
                field.set(src, value);
            } else {
                put(src.getClass().getField(keys.remove(0)).get(src), keys, value);
            }
        }

        @Override
        public Object get(String prop) {
            List<String> keys = new ArrayList<>();
            Collections.addAll(keys, prop.split("\\."));
            return get($, src, keys);
        }

        private static Object get(AQuery $, Object src, List<String> keys) {
            if (keys.size() == 0 || src == null) return src;
            try {
                Field field = src.getClass().getField(keys.remove(0));
                field.setAccessible(true);
                Object value = field.get(src);
                if (value == null) return null;
                return get($, value, keys);
            } catch (NoSuchFieldException | IllegalFormatCodePointException |
                    IllegalAccessException e) {
                $.log.i(e);
                return null;
            }
        }

        @Override
        public void forEach(Action action) {
            if (src == null) return;
            for (Field field : src.getClass().getFields()) {
                field.setAccessible(true);
                try {
                    action.action(src, field, field.get(src));
                } catch (IllegalAccessException e) {
                    $.log.i(e);
                }
            }
        }

        @Override
        public String toString() {
            return toJson();
        }
    }
}
