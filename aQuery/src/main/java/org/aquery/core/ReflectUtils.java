package org.aquery.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 反射工具类
 *
 * @author chpengzh
 */
public class ReflectUtils {

    /***
     * 得到被某注解修饰的全部函数
     *
     * @param type           类类型
     * @param annotationType 注解类类型
     * @return 方法列表
     */
    public static Map<Annotation, Method> getMethodsByAnnotation(
            final Class<? extends Annotation> annotationType, final Class<?> type) {
        final Map<Annotation, Method> methods = new HashMap<>();
        Class<?> clazz = type;
        while (clazz != Object.class) {
            // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by clazz variable,
            // and add those annotated with the specified annotation
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationType)) {
                    methods.put(method.getAnnotation(annotationType), method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /***
     * 根据参数注解构造参数列表
     *
     * @param method   方法
     * @param injector 参数注入规则
     * @return 参数列表
     */
    public static Object[] fillParamsByAnnotations(Method method, ParamInjector injector) {
        Object[] params = new Object[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            List<Annotation> annotations = new ArrayList<>();
            Collections.addAll(annotations, method.getParameterAnnotations()[i]);
            params[i] = injector.onInject(method.getParameterTypes()[i], annotations, i);
        }
        return params;
    }

    public interface ParamInjector {
        Object onInject(Class paramType, List<? extends Annotation> annotations, int position);
    }
}

